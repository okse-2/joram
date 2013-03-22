set terminal postscript eps enhanced
set output "nbrw.eps"
set ytics nomirror
set y2tics
set xlabel "Time (s)"
set ylabel "Messages"
set y2label "Number of workers"
set xrange [0:3500]
set yrange [0:1000]
set y2range [0:10]
#set key outside  horizontal bottom center

plot "prod.dat" using 1:2 with lines lw 2 lt 2 title "Production", \
     "nbrw.dat" using 1:2 with lines lw 2 lt 4 title "# Workers" axis x1y2, \
     "maxl.dat" using 1:2 with lines lw 2 lt 1 title "Max Load";
