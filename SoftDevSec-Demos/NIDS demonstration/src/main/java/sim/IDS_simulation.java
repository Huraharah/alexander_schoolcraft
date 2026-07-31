package sim;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import sim.PcapJsonParser;

/**
 * Single-file controller + launcher that matches FXML: fx:controller="sim.IDS_simulation"
 * - Loads FXML
 * - Wires tables/columns/buttons
 * - Streams CSV rows (from PCAP via tshark)
 * - Applies pluggable detections → assigns Severity + Reason
 * - Pushes each PacketEvent to "All" and auto-filters into severity tabs
 */

public class IDS_simulation extends Application {
	 /* -------------------- FXML-injected controls -------------------- */
    @FXML private TabPane tabs;

    @FXML private TableView<PacketEvent> all_table;
    @FXML private TableColumn<PacketEvent, String> all_time, all_src_ip, all_src_port, all_dst_ip, all_dst_port, all_class, all_reason;

    @FXML private TableView<PacketEvent> info_table, warn_table, alert_table, crit_table;
    @FXML private TableColumn<PacketEvent, String> info_time, info_src_ip, info_src_port, info_dst_ip, info_dst_port, info_class, info_reason;
    @FXML private TableColumn<PacketEvent, String> warn_time, warn_src_ip, warn_src_port, warn_dst_ip, warn_dst_port, warn_class, warn_reason;
    @FXML private TableColumn<PacketEvent, String> alert_time, alert_src_ip, alert_src_port, alert_dst_ip, alert_dst_port, alert_class, alert_reason;
    @FXML private TableColumn<PacketEvent, String> crit_time, crit_src_ip, crit_src_port, crit_dst_ip, crit_dst_port, crit_class, crit_reason;

    @FXML private Button run_btn, reset_btn;

    /* -------------------- Data model & state -------------------- */
    private final ObservableList<PacketEvent> master = FXCollections.observableArrayList();
    private FilteredList<PacketEvent> infoFilter, warnFilter, alertFilter, critFilter;

    // Executors for streaming
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ids-streamer");
        t.setDaemon(true);
        return t;
    });

    // Detection scaffolding
    private final Set<String> blacklist = new HashSet<>();                 // load from ips.txt
    private final Map<String, Deque<Double>> perSrcTimes = new HashMap<>();// for rate/scan windows
    private final Map<String, Set<String>>  perSrcTargets = new HashMap<>();// for horizontal scan

    // Config
    private static final String IPS_TXT  = "ips.txt";       // your malicious list (>= 6)
    private static final int    MAX_ROWS = 3000;            // cap demo rows in the table
    private static double REPLAY_SPEED =
    	    Double.parseDouble(System.getProperty("ids.replaySpeed","0.0"));         // 0 = as-fast-as-possible; >0 = sleep scale
    // Heuristic thresholds (tune for your dataset size)
    private static final int    RATE_THRESHOLD = 120;       // pkts per window
    private static final double RATE_WINDOW_S  = 10.0;      // seconds
    private static final int    SCAN_TARGETS   = 30;        // distinct dst IPs per window
    private static final double SCAN_WINDOW_S  = 30.0;

    /* -------------------- JavaFX lifecycle -------------------- */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ids_simulator.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("IDS Simulator");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    /* -------------------- FXML controller init -------------------- */
    @FXML
    private void initialize() {
        // Column factories (keep everything as String for simplicity)
        bindCols(all_time,  PacketEvent::tsString);
        bindCols(all_src_ip, PacketEvent::srcIp);
        bindCols(all_src_port, PacketEvent::srcPort);
        bindCols(all_dst_ip, PacketEvent::dstIp);
        bindCols(all_dst_port, PacketEvent::dstPort);
        bindCols(all_class, PacketEvent::severityLabel);
        bindCols(all_reason, PacketEvent::reason);

        duplicateColumns(info_time, info_src_ip, info_src_port, info_dst_ip, info_dst_port, info_class, info_reason);
        duplicateColumns(warn_time, warn_src_ip, warn_src_port, warn_dst_ip, warn_dst_port, warn_class, warn_reason);
        duplicateColumns(alert_time, alert_src_ip, alert_src_port, alert_dst_ip, alert_dst_port, alert_class, alert_reason);
        duplicateColumns(crit_time, crit_src_ip, crit_src_port, crit_dst_ip, crit_dst_port, crit_class, crit_reason);

        // Placeholders
        setPlaceholder(all_table,   "No events yet. Click Run Simulation.");
        setPlaceholder(info_table,  "No information events.");
        setPlaceholder(warn_table,  "No warnings.");
        setPlaceholder(alert_table, "No alerts.");
        setPlaceholder(crit_table,  "No critical events.");

        // Master table
        all_table.setItems(master);

        // Severity views via FilteredList (no data duplication)
        infoFilter  = new FilteredList<>(master, sevEquals(Severity.INFO));
        warnFilter  = new FilteredList<>(master, sevEquals(Severity.WARNING));
        alertFilter = new FilteredList<>(master, sevEquals(Severity.ALERT));
        critFilter  = new FilteredList<>(master, sevEquals(Severity.CRITICAL));

        info_table.setItems(infoFilter);
        warn_table.setItems(warnFilter);
        alert_table.setItems(alertFilter);
        crit_table.setItems(critFilter);

        // Wire buttons
        run_btn.setOnAction(e -> runSimulation());
        reset_btn.setOnAction(e -> resetSimulation());

        // Load blacklist once
        loadBlacklist(IPS_TXT);
    }

    /* -------------------- UI helpers -------------------- */
    private void bindCols(TableColumn<PacketEvent, String> col, java.util.function.Function<PacketEvent,String> getter) {
        col.setCellValueFactory(cd -> javafx.beans.binding.Bindings.createStringBinding(() -> getter.apply(cd.getValue())));
    }
    private void duplicateColumns(TableColumn<PacketEvent, String> time,
                                  TableColumn<PacketEvent, String> sip,
                                  TableColumn<PacketEvent, String> sport,
                                  TableColumn<PacketEvent, String> dip,
                                  TableColumn<PacketEvent, String> dport,
                                  TableColumn<PacketEvent, String> sev,
                                  TableColumn<PacketEvent, String> reason) {
        bindCols(time,   PacketEvent::tsString);
        bindCols(sip,    PacketEvent::srcIp);
        bindCols(sport,  PacketEvent::srcPort);
        bindCols(dip,    PacketEvent::dstIp);
        bindCols(dport,  PacketEvent::dstPort);
        bindCols(sev,    PacketEvent::severityLabel);
        bindCols(reason, PacketEvent::reason);
    }
    private void setPlaceholder(TableView<?> tv, String text) {
        tv.setPlaceholder(new Label(text));
        tv.setFixedCellSize(24); // smoother large lists
    }
    private Predicate<PacketEvent> sevEquals(Severity s) {
        return pe -> pe.severity == s;
    }

    /* -------------------- Run / Reset -------------------- */
    private volatile boolean running = false;

    private void runSimulation() {
    	if (running) return;
        running = true;
        System.out.println("Running simulation...");

        worker.submit(() -> {
        	System.out.println("worker submitted");
            try {
                // 1) load all packets from resources via the single parser
            	System.out.println("loading packets");
                List<PcapJsonParser.Packet> packets =
                        PcapJsonParser.loadPacketsFromResource("packet.json");
                System.out.println("Loaded " + packets.size() + " packets total");
                long usable = packets.stream()
                        .filter(pp -> pp.srcIp != null && pp.dstIp != null && pp.srcPort != null && pp.dstPort != null)
                        .count();
                System.out.println("Usable (IPv4+TCP/UDP with ports): " + usable);

                // 2) optionally pace, cap, and classify
                int pushed = 0;
                double firstTs = -1;
                
                System.out.println("Starting load loop");

                double prevEpoch = -1;

                for (PcapJsonParser.Packet p : packets) {
                    if (!running) break;

                    // basic sanity: need IPv4 + ports (parser already does most)
                    if (p.srcIp == null || p.dstIp == null || p.srcPort == null || p.dstPort == null) {
                        continue;
                    }

                    double epoch = (p.timestamp != null)
                            ? p.timestamp.getEpochSecond() + p.timestamp.getNano() / 1_000_000_000.0
                            : 0.0;

                    if (firstTs < 0) firstTs = epoch;

                    // inside the for-loop, after you compute `epoch`
                    if (REPLAY_SPEED > 0) {
                        if (prevEpoch >= 0) {
                            double deltaS = epoch - prevEpoch;            // inter-arrival seconds
                            if (deltaS < 0) deltaS = 0;                   // guard out-of-order
                            // REPLAY_SPEED = 1.0 → real time
                            // REPLAY_SPEED = 2.0 → 2× faster (half the wait)
                            // REPLAY_SPEED = 0.5 → 2× slower (double the wait)
                            long sleepMs = (long) Math.round((deltaS / REPLAY_SPEED) * 1000.0);

                            // Windows timer granularity is ~1–15ms; sub-ms waits won’t happen.
                            // To avoid thousands of 0ms sleeps, only sleep when ≥1ms:
                            if (sleepMs >= 1) Thread.sleep(sleepMs);
                        }
                        prevEpoch = epoch;
                    }

                    // 3) run detections (re-use your existing logic)
                    DetectionResult det = detect_fromParsed(p, epoch);

                    // 4) map to the UI row model the tables expect
                    PacketEvent event = new PacketEvent(
                            epoch,
                            p.srcIp, String.valueOf(p.srcPort),
                            p.dstIp, String.valueOf(p.dstPort),
                            det.severity, det.reason
                    );

                    Platform.runLater(() -> {
                        if (master.size() >= MAX_ROWS) master.remove(0);
                        master.add(event);
                        if (det.severity == Severity.CRITICAL) {
                            tabs.getSelectionModel().select(
                                    tabs.getTabs().stream()
                                            .filter(t -> "Critical".equals(t.getText()))
                                            .findFirst()
                                            .orElse(tabs.getTabs().getFirst())
                            );
                        }
                    });

                    if (++pushed >= MAX_ROWS) break;
                }
                System.out.println("Loop complete");
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                running = false;
            }
        });
    }

    private void resetSimulation() {
        running = false;
        Platform.runLater(master::clear);

        // Clear detection state
        perSrcTimes.clear();
        perSrcTargets.clear();
    }

    private void slide(Deque<Double> times, double now, double windowS) {
        times.addLast(now);
        while (!times.isEmpty() && (now - times.peekFirst()) > windowS) {
            times.removeFirst();
        }
    }

    // Simple “distinct targets in window” tracking.
    // For a precise sliding set, you’d pair dst with timestamp and evict by age; here we reset set opportunistically.
    private void slideTargets(Set<String> targets, String dstIp, double now, String srcIp) {
        targets.add(dstIp);
        // Lightweight decay: occasionally reset to avoid unbounded growth (demo-friendly)
        if (targets.size() > SCAN_TARGETS * 3) {
            targets.clear();
            targets.add(dstIp);
        }
    }

    /* -------------------- IO helpers -------------------- */
    private void loadBlacklist(String fileName) {
        blacklist.clear();
        int count = 0;

        // 1) Try classpath (src/main/resources)
        try (InputStream is = getClass().getResourceAsStream("/" + fileName)) {
            if (is != null) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8))) {
                    for (String line; (line = br.readLine()) != null; ) {
                        if (!line.isEmpty() && line.charAt(0) == '\uFEFF') line = line.substring(1); // strip BOM
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) continue;
                        blacklist.add(line);
                        count++;
                    }
                }
            }
        } catch (Exception ignored) { }

        // 2) Fallback: plain file next to where you run Maven
        if (count == 0) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(fileName), java.nio.charset.StandardCharsets.UTF_8))) {
                for (String line; (line = br.readLine()) != null; ) {
                    if (!line.isEmpty() && line.charAt(0) == '\uFEFF') line = line.substring(1);
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    blacklist.add(line);
                    count++;
                }
            } catch (IOException e) {
                System.err.println("Could not load blacklist " + fileName + ": " + e.getMessage());
            }
        }

        System.out.println("Loaded blacklist entries: " + count);
    }


    

    /* -------------------- Small data types (inner for single-file convenience) -------------------- */
    enum Severity { INFO, WARNING, ALERT, CRITICAL }

    static final class DetectionResult {
        final Severity severity;
        final String reason;
        DetectionResult(Severity s, String r){ this.severity=s; this.reason=r; }
    }

   
    public static final class PacketEvent {
        private final double epoch;
        private final String srcIp, srcPort, dstIp, dstPort;
        private final Severity severity;
        private final String reason;

        public PacketEvent(double epoch, String srcIp, String srcPort, String dstIp, String dstPort, Severity severity, String reason) {
            this.epoch = epoch;
            this.srcIp = nz(srcIp);
            this.srcPort = nz(srcPort);
            this.dstIp = nz(dstIp);
            this.dstPort = nz(dstPort);
            this.severity = severity;
            this.reason = nz(reason);
        }
        public String tsString() { return Instant.ofEpochSecond((long)epoch).toString(); } // readable-ish
        public String srcIp()    { return srcIp; }
        public String srcPort()  { return srcPort; }
        public String dstIp()    { return dstIp; }
        public String dstPort()  { return dstPort; }
        public String severityLabel() { return severity.name(); }
        public String reason()   { return reason; }
        private static String nz(String s){ return (s==null || s.isEmpty()) ? "-" : s; }
    }

    // quick “sensitive ports” set for demo scoring
    private static final Set<Integer> SENSITIVE_PORTS = Set.of(
        22, 23, 25, 53, 80, 110, 139, 143, 389, 445, 465, 587, 593, 636, 1433, 1521, 2049,
        3306, 3389, 4444, 5432, 5900, 5985, 5986, 6379, 8080, 9000
    );

    // keep using your perSrcTimes/perSrcTargets maps

    private DetectionResult detect_fromParsed(PcapJsonParser.Packet p, double epoch) {
    	 String sIp = p.srcIp == null ? "" : p.srcIp.trim();
    	 String dIp = p.dstIp == null ? "" : p.dstIp.trim();
        // 0) Critical if hits blacklist
        if (blacklist.contains(sIp) || blacklist.contains(dIp)) {
            return new DetectionResult(Severity.CRITICAL, "Blacklisted IP match");
        }

        // 1) Sliding rate (per source)
        Deque<Double> times = perSrcTimes.computeIfAbsent(p.srcIp, k -> new ArrayDeque<>());
        slide(times, epoch, RATE_WINDOW_S);
        int rateNow = times.size();
        if (rateNow >= RATE_THRESHOLD) {
            return new DetectionResult(Severity.WARNING, "High rate from " + p.srcIp + " (" + rateNow + "/" + (int)RATE_WINDOW_S + "s)");
        }

        // 2) Horizontal scan (distinct targets per source in window)
        Set<String> targets = perSrcTargets.computeIfAbsent(p.srcIp, k -> new HashSet<>());
        slideTargets(targets, p.dstIp, epoch, p.srcIp);
        int targetsNow = targets.size();
        if (targetsNow >= SCAN_TARGETS) {
            return new DetectionResult(Severity.WARNING, "Many targets from " + p.srcIp + " (" + targetsNow + "/" + (int)SCAN_WINDOW_S + "s)");
        }

        // 3) Sensitive dest ports bump severity (nice for demos)
        if (p.dstPort != null && SENSITIVE_PORTS.contains(p.dstPort)) {
            // escalate a bit if also external-looking
            boolean isRfc1918Dst = p.dstIp.startsWith("10.") || p.dstIp.startsWith("192.168.") || p.dstIp.startsWith("172.16.")
                                 || p.dstIp.startsWith("172.17.") || p.dstIp.startsWith("172.18.") || p.dstIp.startsWith("172.19.")
                                 || p.dstIp.startsWith("172.2")   || p.dstIp.startsWith("172.3");
            Severity s = isRfc1918Dst ? Severity.WARNING : Severity.ALERT;
            return new DetectionResult(s, "Sensitive port " + p.dstPort + " (" + p.protocol + ")");
        }

        // 4) Light protocol hints
        if ("DNS".equals(p.protocol) || "HTTP".equals(p.protocol) || "TLS".equals(p.protocol) || "SMB".equals(p.protocol)) {
            return new DetectionResult(Severity.INFO, p.protocol);
        }

        return new DetectionResult(Severity.INFO, "Normal");
    }
}