Spearman results for Page rank with following parameters:
one iteration with lambda 	= 0.10 : 0.42362299171841833
two iterations with lambda 	= 0.10 : 0.4235195068702069
one iteration with lambda 	= 0.90 : 0.42263337513849597
two iterations with lambda 	= 0.90 : 0.4226299921470178

Since Spearman co-efficient must be as close as possible in value to 1, we chose the Page Rank algorithm with one iteration,
and value of lambda as 0.10

The corpus graph is generated and stored in file data/index/corpusGraph.dat
The Page rank and Numviews data is stored in data/index/pageRankMap.dat and data/index/numViewsMap.dat respectively.

Hence, to compute Spearman co-efficient run the following:
$ java edu.nyu.cs.cs2580.Spearman data/index/pageRankMap.dat data/index/numViewsMap.dat

Thus Spearman rank correlation co-efficient = 0.42362299171841833