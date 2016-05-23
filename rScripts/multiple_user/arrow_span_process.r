### Arrow Span Processing Script ###
### Author: James Stone ###
### Version: 1.0 ###
### For use with the arrow span task available at http://www.cognitivetools.uk ###

#required library
library(stringr)

# Set the working directory of R to the folder that hosts the output files from the arrow span task #


### function that takes the data frame from an output file and returns that persons summary info ###

arrow_span_processing <- function(data.frame) {
	
	num.trials <- nrow(data.frame)
	
	fta.score <- 0 
	for (i in 1:num.trials) {
		if (data.frame$trial.arrow_span.result[i] == "success") {
			fta.score <- fta.score + data.frame$trial.arrow_span.load[i]
		}
	}
	
	#highest load with a fully correct response
	if (length(data.frame$trial.arrow_span.load[data.frame$trial.arrow_span.result == "success"]) == 0) {
		#no successes
		max.span <- NA
	} else {
		max.span <- max(data.frame$trial.arrow_span.load[data.frame$trial.arrow_span.result == "success"]) 
	}
	
	#span two trials correct
	if (nrow(subset(data.frame, trial.arrow_span.load == 2)) == 0) {
		#then there were no span size 2 trials, therefore
		span.2.corr <- NA
	} else {
		span.2.corr <- sum(data.frame$trial.arrow_span.points[data.frame$trial.arrow_span.load == 2])
	}
	#span three trials correct
	if (nrow(subset(data.frame, trial.arrow_span.load == 3)) == 0) {
		#then there were no span size 3 trials, therefore
		span.3.corr <- NA
	} else {
		span.3.corr <- sum(data.frame$trial.arrow_span.points[data.frame$trial.arrow_span.load == 3])
	}	
	#span four trials correct
	if (nrow(subset(data.frame, trial.arrow_span.load == 4)) == 0) {
		#then there were no span size 4 trials, therefore
		span.4.corr <- NA
	} else {
		span.4.corr <- sum(data.frame$trial.arrow_span.points[data.frame$trial.arrow_span.load == 4])
	}	
	#span five trials correct
	if (nrow(subset(data.frame, trial.arrow_span.load == 5)) == 0) {
		#then there were no span size 5 trials, therefore
		span.5.corr <- NA
	} else {
		span.5.corr <- sum(data.frame$trial.arrow_span.points[data.frame$trial.arrow_span.load == 5])
	}
	#span six trials correct
	if (nrow(subset(data.frame, trial.arrow_span.load == 6)) == 0) {
		#then there were no span size 6 trials, therefore
		span.6.corr <- NA
	} else {
		span.6.corr <- sum(data.frame$trial.arrow_span.points[data.frame$trial.arrow_span.load == 6])
	}	
	#span seven trials correct
	if (nrow(subset(data.frame, trial.arrow_span.load == 7)) == 0) {
		#then there were no span size 7 trials, therefore
		span.7.corr <- NA
	} else {
		span.7.corr <- sum(data.frame$trial.arrow_span.points[data.frame$trial.arrow_span.load == 7])
	}	
	#span eight trials correct
	if (nrow(subset(data.frame, trial.arrow_span.load == 8)) == 0) {
		#then there were no span size 8 trials, therefore
		span.8.corr <- NA
	} else {
		span.8.corr <- sum(data.frame$trial.arrow_span.points[data.frame$trial.arrow_span.load == 8])
	}
	#span nine trials correct
	if (nrow(subset(data.frame, trial.arrow_span.load == 9)) == 0) {
		#then there were no span size 9 trials, therefore
		span.9.corr <- NA
	} else {
		span.9.corr <- sum(data.frame$trial.arrow_span.points[data.frame$trial.arrow_span.load == 9])
	}
	
	
	###finally, to get prop score, and number individual successes need to break down the recall 
	###segment to a per element basis, not the per trial basis we get as output.
	
	#create a new data frame where every row is one response within a trial
	
	#number of rows will be the sum of the loads
	num.elements <- sum(data.frame$trial.arrow_span.load)
	
	ID <- numeric(num.elements)
	trial.no <- numeric(num.elements)
	load <- numeric(num.elements)
	angle.question <- numeric(num.elements)
	angle.response <- numeric(num.elements)
	length.question <- numeric(num.elements)
	length.response <- numeric(num.elements)
	length.success <- numeric(num.elements)
	angle.success <- numeric(num.elements)
	overall.success <- numeric(num.elements)
	counter <- 1
	
	for (i in 1:nrow(data.frame)) {
	
		num_elements <- data.frame$trial.arrow_span.load[i] * 2
		tmp_question <- as.character(data.frame$trial.arrow_span.question[i])
		tmp_response <- as.character(data.frame$trial.arrow_span.response[i])
		q <- str_split_fixed(tmp_question,", ", num_elements)
		r <- str_split_fixed(tmp_response,", ", num_elements)
		
		if (nchar(q[1]) == 2) {
		  q[1] <- substr(q[1],2,2)
		} else if (nchar(q[1]) == 3) {
		  q[1] <- substr(q[1],2,3)
		}
		
		if (nchar(r[1]) == 2) {
		  r[1] <- substr(r[1],2,2)
		} else if (nchar(r[1]) == 3) {
		  r[1] <- substr(r[1],2,3)
		}
		
		if (nchar(q[num_elements]) == 2) {
		  q[num_elements] <- substr(q[num_elements],1,1)
		} else if (nchar(q[num_elements]) == 3) {
		  q[num_elements] <- substr(q[num_elements],1,2)
		} else if (nchar(q[num_elements]) == 4) {
		  q[num_elements] <- substr(q[num_elements],1,3)
		}
		
		if (nchar(r[num_elements]) == 2) {
		  r[num_elements] <- substr(r[num_elements],1,1)
		} else if (nchar(r[num_elements]) == 3) {
		  r[num_elements] <- substr(r[num_elements],1,2)
		} else if (nchar(r[num_elements]) == 4) {
		  r[num_elements] <- substr(r[num_elements],1,3)
		}
		
		q.lengths.vec <- as.numeric(q[1:data.frame$trial.arrow_span.load[i]])
		q.angles.vec <- as.numeric(q[(data.frame$trial.arrow_span.load[i]+1):num_elements])
		r.lengths.vec <- as.numeric(r[1:data.frame$trial.arrow_span.load[i]])
		r.angles.vec <- as.numeric(r[(data.frame$trial.arrow_span.load[i]+1):num_elements])
		
		for (j in 1:data.frame$trial.arrow_span.load[i]) {
			ID[counter] <- data.frame$user.name[1]
			trial.no[counter] <- data.frame$trial.arrow_span.trialNo[i]
			load[counter] <- data.frame$trial.arrow_span.load[i]
			angle.question[counter] <- q.angles.vec[j]
			angle.response[counter] <- r.angles.vec[j]
			length.question[counter] <- q.lengths.vec[j]
			length.response[counter] <- r.lengths.vec[j]
			angle.success[counter] <- q.angles.vec[j] == r.angles.vec[j]
			length.success[counter] <- q.lengths.vec[j] == r.lengths.vec[j]
			overall.success[counter] <- q.angles.vec[j] == r.angles.vec[j] & q.lengths.vec[j] == r.lengths.vec[j]
			counter <- counter + 1
		}
	}	
	
	data.frame.indi <- as.data.frame(cbind(ID,trial.no,load,angle.question,angle.response,length.question,length.response,
	angle.success,length.success,overall.success))
	
	number.successes <- sum(data.frame.indi$overall.success)
	
	props <- numeric(num.trials)
	
	for (i in 1:num.trials) {
		this.trial.load <- nrow(subset(data.frame.indi, trial.no == i))
		this.trial.successes <- sum(data.frame.indi$overall.success[data.frame.indi$trial.no == i])
		props[i] <- this.trial.successes / this.trial.load
	}
	
	prop.score <- mean(props)
	
	
	
	p.summary <- c(fta.score, prop.score, number.successes, 
	max.span, span.2.corr, span.3.corr, span.4.corr, span.5.corr, span.6.corr, span.7.corr, span.8.corr, 
	span.9.corr)
	
	return(p.summary)	
	
}

##now can use that function on all our data files for arrow span
#put all arrow span files into one folder and set that folder as working directory

compile_arrow_span_data <- function() {

	multiple.sessions <- character()
	filenames <- list.files()
	arrow.span.data <- matrix(nrow=length(filenames), ncol=12) #ncol needs to be number of values in p.summary above
	user <- character()
	
	for (i in 1:length(filenames)) {
		
		tmp.df <- read.csv(filenames[i])
		user[i] <- toString(tmp.df$user.name[1])
		
		if (length(unique(tmp.df$session.id)) > 1) {
			multiple.sessions[length(multiple.sessions) + 1] <- filenames[i]
		}

		tmp.arrow.summary <- arrow_span_processing(tmp.df)
		
		arrow.span.data[i, ] <- tmp.arrow.summary
	
	}
	
	arrow.span.data <- as.data.frame(arrow.span.data)
	arrow.span.data$user <- user
	
	rot.names <- c("fta.score", "prop.score", "number.successes", 
	"max.span", "span.2.corr", "span.3.corr", "span.4.corr", "span.5.corr", "span.6.corr", "span.7.corr", "span.8.corr", 
	"span.9.corr", "user")

	names(arrow.span.data) <- rot.names
	
	if (length(multiple.sessions > 0)) {
		cat("The following files contained trials with more than one session id: \n", multiple.sessions)
	}
	
	return(arrow.span.data)	

}

arrow.span.data <- compile_arrow_span_data()

rm(compile_arrow_span_data, arrow_span_processing)