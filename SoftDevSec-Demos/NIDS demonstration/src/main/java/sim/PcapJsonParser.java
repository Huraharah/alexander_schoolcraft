package sim;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.time.Instant;
import java.util.*;

public class PcapJsonParser {

    public static class Packet {
        public Instant timestamp;
        public String srcIp;
        public String dstIp;
        public Integer srcPort;
        public Integer dstPort;
        public String rawProtocol;     // raw field from JSON
        public String protocol;        // normalized protocol
        // optional: original map for fields you didn't model explicitly
        public Map<String, Object> meta;
    }

    private static final Map<String, String> PROTOCOL_MAP = buildProtocolMap();

    private static Map<String,String> buildProtocolMap(){
        Map<String,String> m = new HashMap<>();
        // common maps (lowercased keys)
        m.put("tcp", "TCP");
        m.put("udp", "UDP");
        m.put("icmp", "ICMP");
        m.put("arp", "ARP");
        m.put("http", "HTTP");
        m.put("https", "TLS");
        m.put("tls", "TLS");
        m.put("ssl", "TLS");
        m.put("dns", "DNS");
        m.put("dhcp", "DHCP");
        m.put("nbns", "NBNS");
        m.put("netbios", "NBNS");
        m.put("llmnr", "LLMNR");
        m.put("mdns", "MDNS");
        m.put("smb", "SMB");
        m.put("smb2", "SMB");
        m.put("ldap", "LDAP");
        m.put("cldap", "CLDAP");
        m.put("ntp", "NTP");
        m.put("snmp", "SNMP");
        m.put("ssdp", "SSDP");
        m.put("sip", "SIP/RTP");
        m.put("rtp", "SIP/RTP");
        m.put("igmp", "IGMP");
        m.put("ipv6", "IPV6");
        m.put("icmpv6","IPV6");
        return m;
    }

    static String normalizeProtocol(String raw){
        if(raw==null) return "OTHER";
        String s = raw.trim().toLowerCase();
        // sometimes tshark uses combined labels like "DNS: response" or "HTTP/2"
        s = s.split("[\\s/:]+")[0]; // take first token
        return PROTOCOL_MAP.getOrDefault(s, "OTHER");
    }

    /**
     * Load packets from a JSON file in resources (e.g. /packet.json).
     * The JSON may be an array of objects or newline-delimited objects.
     */
    public static List<Packet> loadPacketsFromResource(String resourceName) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream in = PcapJsonParser.class.getResourceAsStream("/" + resourceName);
        if (in == null) throw new IllegalArgumentException("Resource not found: " + resourceName);

        // Read as line-delimited JSON; fallback to array if needed
        List<Map<String,Object>> rows = new ArrayList<>();
        try (Scanner sc = new Scanner(in)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (!line.isEmpty()) {
                    rows.add(mapper.readValue(line, new TypeReference<Map<String,Object>>(){}));
                }
            }
        }
        if (rows.isEmpty()) {
            // try array form
            in = PcapJsonParser.class.getResourceAsStream("/" + resourceName);
            rows = mapper.readValue(in, new TypeReference<List<Map<String,Object>>>(){});
        }

        List<Packet> out = new ArrayList<>();
        for (Map<String,Object> r : rows) {
            Packet p = new Packet();

            // timestamp (top-level "timestamp" or "frame_time_epoch")
            Object t = r.get("frame_time_epoch");
            if (t == null) t = r.get("timestamp");
            if (t != null) {
                try {
                    double epoch = Double.parseDouble(t.toString());
                    long secs = (long) epoch;
                    long nanos = (long) ((epoch - secs) * 1_000_000_000L);
                    p.timestamp = Instant.ofEpochSecond(secs, nanos);
                } catch (Exception ignore) {}
            }

            // ek layout: everything interesting lives under "layers"
            Map<String,Object> layers = asMap(r.get("layers"));

            // IPs
            p.srcIp = firstText(layers, "ip_src", "ip.src");
            p.dstIp = firstText(layers, "ip_dst", "ip.dst");

            // Ports (prefer TCP, else UDP)
            Integer tcpSp = firstInt(layers, "tcp_srcport", "tcp.srcport");
            Integer tcpDp = firstInt(layers, "tcp_dstport", "tcp.dstport");
            Integer udpSp = firstInt(layers, "udp_srcport", "udp.srcport");
            Integer udpDp = firstInt(layers, "udp_dstport", "udp.dstport");

            if (tcpSp != null && tcpDp != null) {
                p.srcPort = tcpSp; p.dstPort = tcpDp; p.rawProtocol = "TCP";
            } else if (udpSp != null && udpDp != null) {
                p.srcPort = udpSp; p.dstPort = udpDp; p.rawProtocol = "UDP";
            }

            // Protocol column (varies by exporter)
            if (p.rawProtocol == null) {
                String col = firstText(layers, "protocol", "_ws.col.Protocol", "_ws_col_Protocol", "highest_layer");
                p.rawProtocol = (col != null && !col.isBlank()) ? col : null;
            }
            p.protocol = normalizeProtocol(p.rawProtocol);

            p.meta = r;

            // keep only packets with complete addressing
            if (p.srcIp != null && p.dstIp != null && p.srcPort != null && p.dstPort != null) {
                out.add(p);
            }
        }
        return out;
    }

    // ---- helpers (add to the same class) ----
    @SuppressWarnings("unchecked")
    private static Map<String,Object> asMap(Object o) {
        return (o instanceof Map) ? (Map<String,Object>) o : Collections.emptyMap();
    }

    private static String firstText(Map<String,Object> layers, String... keys) {
        for (String k : keys) {
            Object v = layers.get(k);
            if (v instanceof List<?> l && !l.isEmpty()) return Objects.toString(l.get(0), null);
            if (v instanceof String s && !s.isBlank())  return s;
        }
        return null;
    }

    private static Integer firstInt(Map<String,Object> layers, String... keys) {
        String s = firstText(layers, keys);
        if (s == null) return null;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
    }

    // ------------------ Example simple detections (not used in this demonstration, but for visibility) ------------------
    public static void detectPortScans(List<Packet> packets, int timeWindowSeconds, int portThreshold){
        // windowing naive example: group by srcIp and time bucket (rounded epoch seconds)
        Map<String, Map<Long, Set<Integer>>> map = new HashMap<>();
        for(Packet p : packets){
            if(p.srcIp==null) continue;
            long bucket = p.timestamp != null ? p.timestamp.getEpochSecond() / timeWindowSeconds : 0;
            map.computeIfAbsent(p.srcIp, k -> new HashMap<>())
               .computeIfAbsent(bucket, k -> new HashSet<>());
            if(p.dstPort != null) map.get(p.srcIp).get(bucket).add(p.dstPort);
        }
        for(Map.Entry<String, Map<Long, Set<Integer>>> e : map.entrySet()){
            String src = e.getKey();
            for(Map.Entry<Long, Set<Integer>> b : e.getValue().entrySet()){
                if(b.getValue().size() >= portThreshold){
                    System.out.printf("ALERT port-scan: %s contacted %d dst ports in window %d%n",
                            src, b.getValue().size(), b.getKey());
                }
            }
        }
    }

    public static void detectHorizontalScan(List<Packet> packets, int timeWindowSeconds, int ipThreshold){
        Map<String, Map<Integer, Map<Long, Set<String>>>> map = new HashMap<>();
        for(Packet p : packets){
            if(p.srcIp==null || p.dstPort==null) continue;
            long bucket = p.timestamp != null ? p.timestamp.getEpochSecond() / timeWindowSeconds : 0;
            map.computeIfAbsent(p.srcIp, k -> new HashMap<>())
               .computeIfAbsent(p.dstPort, k -> new HashMap<>())
               .computeIfAbsent(bucket, k -> new HashSet<>())
               .add(p.dstIp);
        }
        for(Map.Entry<String, Map<Integer, Map<Long, Set<String>>>> e : map.entrySet()){
            String src = e.getKey();
            for(Map.Entry<Integer, Map<Long, Set<String>>> portMap : e.getValue().entrySet()){
                int port = portMap.getKey();
                for(Map.Entry<Long, Set<String>> b : portMap.getValue().entrySet()){
                    if(b.getValue().size() >= ipThreshold){
                        System.out.printf("ALERT horizontal-scan: %s scanned %d dst IPs on port %d in bucket %d%n",
                                src, b.getValue().size(), port, b.getKey());
                    }
                }
            }
        }
    }
}
