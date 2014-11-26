# Gnuplot script
set terminal postscript eps enhanced
set output "maxl.eps"
set xlabel "Time (s)"
set ylabel "Messages"
set xrange [0:4500]
set yrange [0:20000]

plot 'maxl.dat' using 1:2 title "Maximum load" with lines lw 2 lc 1;
     
