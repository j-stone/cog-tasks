### Symmetry Span Processing Script ###
### Author: James Stone ###
### Version: 1.0 ###
### For use with the symmetry span task available at http://www.cognitivetools.uk/ ###

#required library
library(stringr)

# Set the working directory of R to the folder that hosts the output files from the symmetry span task #


### function that takes the data frame from an output file and returns that persons summary info ###

symm_span_processing <- function(data.frame) {

	#split storage rows from processing rows
	df.recall <- subset(data.frame, trial.name == "module.list.comp.exec.symm_span")
	df.processing <- subset(data.frame, trial.name =="module.list.comp.exec.symm_processing")
	
	num.trials <- nrow(df.recall)
	
	fta.score <- 0 
	for (i in 1:num.trials) {
		if (df.recall$trial.symm_span.result[i] == "success") {
			fta.score <- fta.score + df.recall$trial.symm_span.load[i]
		}
	}
	
	#highest load with a fully correct response
	if (length(df.recall$trial.symm_span.load[df.recall$trial.symm_span.result == "success"]) == 0) {
		#no successes
		max.span <- NA
	} else {
		max.span <- max(df.recall$trial.symm_span.load[df.recall$trial.symm_span.result == "success"]) 
	}	

	#span two trials correct
	if (nrow(subset(df.recall, trial.symm_span.load == 2)) == 0) {
		#then there were no span size 2 trials, therefore
		span.2.corr <- NA
	} else {
		span.2.corr <- sum(df.recall$trial.symm_span.points[df.recall$trial.symm_span.load == 2])
	}
	#span three trials correct
	if (nrow(subset(df.recall, trial.symm_span.load == 3)) == 0) {
		#then there were no span size 3 trials, therefore
		span.3.corr <- NA
	} else {
		span.3.corr <- sum(df.recall$trial.symm_span.points[df.recall$trial.symm_span.load == 3])
	}	
	#span four trials correct
	if (nrow(subset(df.recall, trial.symm_span.load == 4)) == 0) {
		#then there were no span size 4 trials, therefore
		span.4.corr <- NA
	} else {
		span.4.corr <- sum(df.recall$trial.symm_span.points[df.recall$trial.symm_span.load == 4])
	}	
	#span five trials correct
	if (nrow(subset(df.recall, trial.symm_span.load == 5)) == 0) {
		#then there were no span size 5 trials, therefore
		span.5.corr <- NA
	} else {
		span.5.corr <- sum(df.recall$trial.symm_span.points[df.recall$trial.symm_span.load == 5])
	}
	#span six trials correct
	if (nrow(subset(df.recall, trial.symm_span.load == 6)) == 0) {
		#then there were no span size 6 trials, therefore
		span.6.corr <- NA
	} else {
		span.6.corr <- sum(df.recall$trial.symm_span.points[df.recall$trial.symm_span.load == 6])
	}	
	#span seven trials correct
	if (nrow(subset(df.recall, trial.symm_span.load == 7)) == 0) {
		#then there were no span size 7 trials, therefore
		span.7.corr <- NA
	} else {
		span.7.corr <- sum(df.recall$trial.symm_span.points[df.recall$trial.symm_span.load == 7])
	}	
	#span eight trials correct
	if (nrow(subset(df.recall, trial.symm_span.load == 8)) == 0) {
		#then there were no span size 8 trials, therefore
		span.8.corr <- NA
	} else {
		span.8.corr <- sum(df.recall$trial.symm_span.points[df.recall$trial.symm_span.load == 8])
	}
	#span nine trials correct
	if (nrow(subset(df.recall, trial.symm_span.load == 9)) == 0) {
		#then there were no span size 9 trials, therefore
		span.9.corr <- NA
	} else {
		span.9.corr <- sum(df.recall$trial.symm_span.points[df.recall$trial.symm_span.load == 9])
	}
	
	#get processing accuracy
	proc.points <- numeric(nrow(df.processing))
	for (i in 1:nrow(df.processing)) {
		if (df.processing$trial.symm_processing.result[i] == "success") {
			proc.points[i] <- 1
		} else {
			proc.points[i] <- 0
		}
	}
	df.processing$points <- proc.points
	
	processing.accuracy <- sum(df.processing$points) / nrow(df.processing) 
	processing.median.rt <- median(df.processing$trial.symm_processing.durationTime)	

	###finally, to get prop score, and number individual successes need to break down the recall 
	###segment to a per element basis, not the per trial basis we get as output.
	
	#create a new data frame where every row is one response within a trial
	
	#number of rows will be the sum of the loads
	num.elements <- sum(df.recall$trial.symm_span.load)
	
	ID <- numeric(num.elements)
	trial.no <- numeric(num.elements)
	load <- numeric(num.elements)
	question <- numeric(num.elements)
	response <- numeric(num.elements)
	success <- numeric(num.elements)
	counter <- 1
	
	for (i in 1:nrow(df.recall)) {
	
		num_elements <- df.recall$trial.symm_span.load[i]
		tmp_question <- as.character(df.recall$trial.symm_span.question[i])
		tmp_response <- as.character(df.recall$trial.symm_span.response[i])
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
		
		q.vec <- as.numeric(q)
		r.vec <- as.numeric(r)
		
		for (j in 1:df.recall$trial.symm_span.load[i]) {
			ID[counter] <- df.recall$user.yearOfBirth[1]
			trial.no[counter] <- df.recall$trial.symm_span.trialNo[i]
			load[counter] <- df.recall$trial.symm_span.load[i]
			question[counter] <- q.vec[j]
			response[counter] <- r.vec[j]
			success[counter] <- q.vec[j] == r.vec[j]
			counter <- counter + 1
		}
	}
	
	df.recall.indi <- as.data.frame(cbind(ID,trial.no,load,question,response,success))
	
	number.successes <- sum(df.recall.indi$success)
	
	props <- numeric(num.trials)
	
	for (i in 1:num.trials) {
		this.trial.load <- nrow(subset(df.recall.indi, trial.no == i))
		this.trial.successes <- sum(df.recall.indi$success[df.recall.indi$trial.no == i])
		props[i] <- this.trial.successes / this.trial.load
	}
	
	prop.score <- mean(props)
	
	
	
	p.summary <- c(fta.score, prop.score, number.successes, processing.accuracy, processing.median.rt, 
	max.span, span.2.corr, span.3.corr, span.4.corr, span.5.corr, span.6.corr, span.7.corr, span.8.corr, 
	span.9.corr)
	
	return(p.summary)		

}


##now can use that function on all our data files for symm span
#put all symm span files into one folder and set that folder as working directory

compile_symm_span_data <- function() {

	multiple.sessions <- character()
	filenames <- list.files()
	symmetry.span.data <- matrix(nrow=length(filenames), ncol=14) #ncol needs to be number of values in p.summary above
	user <- character()
	
	for (i in 1:length(filenames)) {
		
		tmp.df <- read.csv(filenames[i])
		user[i] <- toString(tmp.df$user.name[1])
		
		if (length(unique(tmp.df$session.id)) > 1) {
			multiple.sessions[length(multiple.sessions) + 1] <- filenames[i]
		}

		tmp.symm.summary <- symm_span_processing(tmp.df)
		
		symmetry.span.data[i, ] <- tmp.symm.summary
	
	}
	
	symmetry.span.data <- as.data.frame(symmetry.span.data)
	symmetry.span.data$user <- user
	
	symm.names <- c("fta.score", "prop.score", "number.successes", "processing.accuracy", "processing.median.rt", 
	"max.span", "span.2.corr", "span.3.corr", "span.4.corr", "span.5.corr", "span.6.corr", "span.7.corr", "span.8.corr", 
	"span.9.corr", "user")

	names(symmetry.span.data) <- symm.names
	
	if (length(multiple.sessions > 0)) {
		cat("The following files contained trials with more than one session id: \n", multiple.sessions)
	}
	
	return(symmetry.span.data)	

}

symmetry.span.data <- compile_symm_span_data()

rm(compile_symm_span_data, symm_span_processing)
