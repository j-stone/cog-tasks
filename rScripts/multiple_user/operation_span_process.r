### Operation Span Processing Script ###
### Author: James stone ###
### Version: 1.0 ###
### For use with the operation span task available at http://www.cognitivetools.uk/ ###

#set the working directory to the folder that is holding the data files from the operation span task


###function that takes the output file for a participant and returns summary information for that person###

operation_span_processing <- function(data.frame) {

	#seperate processing trials from digit span trials
	
	df.recall <- subset(data.frame, !is.na(trial.digit_span.trialNo))
	df.operations <- subset(data.frame, is.na(trial.digit_span.trialNo))


	#standard output form digit span has each element of a trial as a row, 
	#also need to collate these into success/failure for whole trials.
	num.trials <- max(df.recall$trial.digit_span.trialNo)
	#set these vectors up now, to be filled later.
	trial.load <- numeric(num.trials)
	num.corr <- numeric(num.trials)
	prop.corr <- numeric(num.trials)
	trial.success <- numeric(num.trials)
	
	for (i in 1:num.trials) {
		indi_trial <- df.recall$trial.digit_span.result[df.recall$trial.digit_span.trialNo == i]
		numDigits <- length(indi_trial)
		numCorr <- sum(indi_trial == "success")
		trial.success[i] <- numDigits == numCorr
		trial.load[i] <- numDigits
		num.corr[i] <- numCorr
		prop.corr[i] <- numCorr / numDigits
	}	
	
	df.recall.wholeTrial <- as.data.frame(cbind(trial.load, num.corr, prop.corr, trial.success))
	df.recall.wholeTrial <- transform(df.recall.wholeTrial, score = trial.load * trial.success)
	
	#highest load with a fully correct response
	if (length(df.recall.wholeTrial$trial.load[df.recall.wholeTrial$trial.success == 1]) == 0) {
		#no successes
		max.span <- NA
	} else {
		max.span <- max(df.recall.wholeTrial$trial.load[df.recall.wholeTrial$trial.success == 1]) 
	}
	
	#total number of words correctly recalled in correct serial position throughout
	number.successes <- sum(data.frame$trial.digit_span.result == "success")
	#span two trials correct
	if (nrow(subset(df.recall.wholeTrial, trial.load == 2)) == 0) {
		#then there were no span size 2 trials, therefore
		span.2.corr <- NA
	} else {
		span.2.corr <- sum(df.recall.wholeTrial$trial.success[df.recall.wholeTrial$trial.load == 2])
	}
	#span three trials correct
	if (nrow(subset(df.recall.wholeTrial, trial.load == 3)) == 0) {
		#then there were no span size 3 trials, therefore
		span.3.corr <- NA
	} else {
		span.3.corr <- sum(df.recall.wholeTrial$trial.success[df.recall.wholeTrial$trial.load == 3])
	}	
	#span four trials correct
	if (nrow(subset(df.recall.wholeTrial, trial.load == 4)) == 0) {
		#then there were no span size 4 trials, therefore
		span.4.corr <- NA
	} else {
		span.4.corr <- sum(df.recall.wholeTrial$trial.success[df.recall.wholeTrial$trial.load == 4])
	}	
	#span five trials correct
	if (nrow(subset(df.recall.wholeTrial, trial.load == 5)) == 0) {
		#then there were no span size 5 trials, therefore
		span.5.corr <- NA
	} else {
		span.5.corr <- sum(df.recall.wholeTrial$trial.success[df.recall.wholeTrial$trial.load == 5])
	}
	#span six trials correct
	if (nrow(subset(df.recall.wholeTrial, trial.load == 6)) == 0) {
		#then there were no span size 6 trials, therefore
		span.6.corr <- NA
	} else {
		span.6.corr <- sum(df.recall.wholeTrial$trial.success[df.recall.wholeTrial$trial.load == 6])
	}	
	#span seven trials correct
	if (nrow(subset(df.recall.wholeTrial, trial.load == 7)) == 0) {
		#then there were no span size 7 trials, therefore
		span.7.corr <- NA
	} else {
		span.7.corr <- sum(df.recall.wholeTrial$trial.success[df.recall.wholeTrial$trial.load == 7])
	}	
	#span eight trials correct
	if (nrow(subset(df.recall.wholeTrial, trial.load == 8)) == 0) {
		#then there were no span size 8 trials, therefore
		span.8.corr <- NA
	} else {
		span.8.corr <- sum(df.recall.wholeTrial$trial.success[df.recall.wholeTrial$trial.load == 8])
	}
	#span nine trials correct
	if (nrow(subset(df.recall.wholeTrial, trial.load == 9)) == 0) {
		#then there were no span size 9 trials, therefore
		span.9.corr <- NA
	} else {
		span.9.corr <- sum(df.recall.wholeTrial$trial.success[df.recall.wholeTrial$trial.load == 9])
	}	
	
	fta.score <- sum(df.recall.wholeTrial$score)
	prop.score <- mean(df.recall.wholeTrial$prop.corr)
	number.successes <- sum(df.recall$trial.digit_span.result == "success")
	
	processing.accuracy <- sum(df.operations$trial.operation_processing.result == "success") / nrow(df.operations)
	processing.median.rt <- median(df.operations$trial.operation_processing.durationTime)
	
	
	p.summary <- c(fta.score, prop.score, number.successes, processing.accuracy, processing.median.rt, 
	max.span, span.2.corr, span.3.corr, span.4.corr, span.5.corr, span.6.corr, span.7.corr, span.8.corr, 
	span.9.corr)
	
	return(p.summary)
}

##now can use that function on all our data files for operation span
#put all operation span files into one folder and set that folder as working directory

compile_operation_span_data <- function() {

	multiple.sessions <- character()
	filenames <- list.files()
	operation.span.data <- matrix(nrow=length(filenames), ncol=14) #ncol needs to be number of values in p.summary above
	user <- character()
	
	for (i in 1:length(filenames)) {
		
		tmp.df <- read.csv(filenames[i])
		user[i] <- toString(tmp.df$user.name[1])
		
		if (length(unique(tmp.df$session.id)) > 1) {
			multiple.sessions[length(multiple.sessions) + 1] <- filenames[i]
		}

		tmp.os.summary <- operation_span_processing(tmp.df)
		
		operation.span.data[i, ] <- tmp.os.summary
	
	}
	
	operation.span.data <- as.data.frame(operation.span.data)
	operation.span.data$user <- user
	
	os.names <- c("fta.score", "prop.score", "number.successes", "processing.accuracy", "processing.median.rt", 
	"max.span", "span.2.corr", "span.3.corr", "span.4.corr", "span.5.corr", "span.6.corr", "span.7.corr", "span.8.corr", 
	"span.9.corr", "user")

	names(operation.span.data) <- os.names
	
	if (length(multiple.sessions > 0)) {
		cat("The following files contained trials with more than one session id: \n", multiple.sessions)
	}
	
	return(operation.span.data)	

}

operation.span.data <- compile_operation_span_data()

rm(compile_operation_span_data, operation_span_processing)
























