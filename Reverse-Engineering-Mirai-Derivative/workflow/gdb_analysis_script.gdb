set pagination off
set confirm off
set disassembly-flavor intel
set follow-fork-mode child
set detach-on-fork off
set print thread-events off

catch fork
commands
silent
printf "\n========== FORK ==========\n"
printf "EIP: %p\n", $eip
bt 3
continue
end

catch vfork
commands
silent
printf "\n========== VFORK ==========\n"
printf "EIP: %p\n", $eip
bt 3
continue
end

catch syscall mmap2
commands
silent
printf "\n========== MMAP2 ==========\n"
printf "EIP: %p\n", $eip
info proc mappings
continue
end

catch syscall munmap
commands
silent
printf "\n========== MUNMAP ==========\n"
printf "EIP: %p\n", $eip
info proc mappings
continue
end

catch syscall mprotect
commands
silent
printf "\n========== MPROTECT ==========\n"
printf "EIP: %p\n", $eip
info proc mappings
continue
end

catch syscall socketcall
commands
silent
printf "\n========== SOCKETCALL ==========\n"
printf "EIP: %p  SUBCALL(EBX): %#x\n", $eip, $ebx
x/8wx $ecx
continue
end

catch syscall unlink
commands
silent
printf "\n========== UNLINK ==========\n"
printf "EIP: %p\n", $eip
printf "pathname ptr: %p\n", $ebx
x/s $ebx
bt 3
continue
end

catch syscall unlinkat
commands
silent
printf "\n========== UNLINKAT ==========\n"
printf "EIP: %p\n", $eip
printf "dirfd: %#x  pathname ptr: %p  flags: %#x\n", $ebx, $ecx, $edx
x/s $ecx
bt 3
continue
end

starti
printf "\n========== ENTRY POINT ==========\n"
printf "EIP: %p\n", $eip
info proc mappings
continue