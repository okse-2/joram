# Gnuplot script
set terminal postscript eps enhanced
set output "suml.eps"
set xlabel "Time (s)"
set ylabel "Messages"
set xrange [0:4500]
set yrange [0:40000]

plot 'suml.dat' using 1:2 title "Total load" with lines lw 2 lc 1;
     
