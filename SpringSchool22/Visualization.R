
path <- "/Users/ccrowe/github/SpringSchool22/Transition_Dependency_Model_Scores.csv"
data <- read.csv(path)
print(data)

library(ggplot2)
library(tidyr)
library(tibble)
#library(hrbrthemes)
library(dplyr)


# Plot for relationship, cluster transitions, high scores.
ggplot(data, aes(start, end, fill= F.Score)) + geom_tile() + theme(legend.position="none") +  xlab("Origin") + ylab("Destination Cluster") + ggtitle("Cluster History vs. Likelihood of Cluster Transition\nvia a Logistic Regression Model") + theme(legend.position = "bottom")


path <- "/Users/ccrowe/github/SpringSchool22/Cluster_Sizes.csv"
data <- read.csv(path)
print(data)
ggplot(data, aes(x, y, size= Percentile)) + geom_point()


base <- ggplot(data, aes(1, data$Cluster, size = data$Percentile)) + 
  geom_point() + 
  scale_x_continuous(breaks = NULL) + 
  labs(x = NULL, y = NULL, size = NULL)  + theme(legend.position = "Percentile Size of Cluster")

base + 
  scale_radius(limits = c(0, NA), range = c(0, 10)) + 
  ggtitle("Relative Sizes of Clusters 1-7", subtitle = "97% in Cluster 0") + xlab("Cluster's Percentile Size") + ylab("Cluster Number") + 
  scale_y_continuous(breaks = seq(0, 10,1))


# Graph feature importances



library(ggrepel)
library(dplyr)
path <- "/Users/ccrowe/github/SpringSchool22/important_features.csv"
data <- read.csv(path) %>% as_tibble() %>% head(-1)
data$one_order <- factor(data$one_num, level = c("0", "5.33", "16.89", "30.04", "45.36", "230.63", "519.16", "733.08", "2391.86", "11602.87"))
data$two_order <- factor(data$two_num, level = c("0", "1", "1.65", "0.99", "2.28", "12.75", "83.17", "30.78", "1394.39", "11058.7"))
data$one_num = as.character(data$one_num)
data$two_num = as.character(data$two_num)
library("jcolors")
ggplot(data, aes(data$one_order, data$two_order, color= factor(cluster))) + 
  geom_point() + ylab("Second most Important Metric (std dev)") + 
  xlab("Most Important Metric (std dev)") + 
  ggtitle("Plot of Cluster Centers as Standard Deviations from the Mean",
          subtitle = "Top Two Metrics (not to scale)") +
geom_label_repel(aes(label = 
        paste(paste(paste("Cluster ", cluster, sep=" "), one, sep="\n"), two, sep = "\n")),
                       box.padding   = 0.4,
        label.padding = 0.3,
        position="identity",
        max.overlaps = 3,
        force = 6,
                       point.padding = 0) + 
  scale_fill_jcolors_contin("pal2", bias = 0.25) +
  theme(legend.position="none")




path <- "/Users/ccrowe/github/SpringSchool22/engagement_transition_f_score.csv"
data <- read.csv(path)
print(data)

# Plot for relationship, cluster transitions, high scores.
ggplot(data, aes(start, end, fill= F.Score)) + 
  geom_tile() + 
  xlab("Origin") + 
  ylab("Destination Cluster") + 
  ggtitle("F-Score from Predicting Cluster Transitions via Member Engagements") + 
  theme(legend.position = "bottom")


path <- "/Users/ccrowe/github/SpringSchool22/Transition_Feature_Importance_Flat.csv"
data <- read.csv(path)
print(data)

#data$one_order <- factor(data$one_num, level = c("0", "5.33", "16.89", "30.04", "45.36", "230.63", "519.16", "733.08", "2391.86", "11602.87"))
#data$two_order <- factor(data$two_num, level = c("0", "1", "1.65", "0.99", "2.28", "12.75", "83.17", "30.78", "1394.39", "11058.7"))
#data$one_num = as.character(data$one_num)
#data$two_num = as.character(data$two_num)

ggplot(data, aes(data$start, data$end)) + 
  geom_point() +
  ylab("Desination Cluster") + 
  xlab("Start Cluster") + 
  ggtitle("Plot of Metrics Predicting Cluster Transitions") +
  geom_label_repel(aes(label = paste(
                         paste(first, data$first_num, sep=", "),
                       paste(second, second_num, sep=", "), sep="\n")
                       ),
                   box.padding   = 0.5, 
                   point.padding = 0.1) + 
  scale_fill_jcolors_contin("pal2", bias = 1.75)



path <- "/Users/ccrowe/github/SpringSchool22/Transition_Feature_Importance_Flat.csv"
data <- read.csv(path)
print(data)

# Plot for relationship, cluster transitions, high scores.
ggplot(data, aes(start, end, fill= )) + 
  geom_tile() + 
  xlab("Origin") + 
  ylab("Destination Cluster") + 
  ggtitle("F-Score from Predicting Cluster Transitions via Member Engagements") + 
  theme(legend.position = "bottom")








#data %>% select(one_num) %>% max
# Junk
#data %>%
#  select(one_num) %>%
#  rowwise() %>% 
#  mutate(one_num= min(one_num,50))
#data %>%
#  select(one_num) %>%
#  rowwise() %>% 
#  mutate(two_num= min(tw-num,50))
#data$one_num_scaled = fun_range(data$one_num)
#data$two_num_scaled = fun_range(data$two_num)
threshold_100 <- function(num) {
  if(num > 100){
    threshold_100 <- 100
  }else{
    threshold_100 <- num 
  }
}
fun_range <- function(x) {                              # Create user-defined function
  (x - min(x)) / (max(x) - min(x)) * 10
}