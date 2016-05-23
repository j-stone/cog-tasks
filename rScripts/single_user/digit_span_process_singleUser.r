### Digit Span (single user) Processing Script ###
### Author: James Stone ###
### Version: 1.0 ###
### For use with the digit span task available at http://www.cognitivetools.uk ###


#Set the working directory to the folder that holds the digit span datafiles you wish to analyse


### function that takes an output file and returns summary information for that participant
digit_span_processing <- function(data.frame) { 

	#standard output form digit span has each element of a trial as a row, 
	#also need to collate these into success/failure for whole trials.
	num.trials <- max(data.frame$trial.digit_span.trialNo)
	trial.load <- numeric(num.trials)
	num.corr <- numeric(num.trials)
	prop.corr <- numeric(num.trials)
	trial.success <- numeric(num.trials)
	
	for (i in 1:num.trials) {
		indi_trial <- data.frame$trial.digit_span.result[data.frame$trial.digit_span.trialNo == i]
		numDigits <- length(indi_trial)
		numCorr <- sum(indi_trial == "success")
		trial.success[i] <- numDigits == numCorr
		trial.load[i] <- numDigits
		num.corr[i] <- numCorr
		prop.corr[i] <- numCorr / numDigits
	}
	#make that a data.frame
	wholeTrialSuccess <- as.data.frame(cbind(trial.load,num.corr,prop.corr,trial.success))
	wholeTrialSuccess <- transform(wholeTrialSuccess, score = trial.load * trial.success)
	
	#highest load with a fully correct response
	if (length(wholeTrialSuccess$trial.load[wholeTrialSuccess$trial.success == 1]) == 0) {
		#no successes
		max.span <- NA
	} else {
		max.span <- max(wholeTrialSuccess$trial.load[wholeTrialSuccess$trial.success == 1]) 
	}
	
	#total number of words correctly recalled in correct serial position throughout
	number.successes <- sum(data.frame$trial.digit_span.result == "success")
	#span two trials correct
	if (nrow(subset(wholeTrialSuccess, trial.load == 2)) == 0) {
		#then there were no span size 2 trials, therefore
		span.2.corr <- NA
	} else {
		span.2.corr <- sum(wholeTrialSuccess$trial.success[wholeTrialSuccess$trial.load == 2])
	}
	#span three trials correct
	if (nrow(subset(wholeTrialSuccess, trial.load == 3)) == 0) {
		#then there were no span size 3 trials, therefore
		span.3.corr <- NA
	} else {
		span.3.corr <- sum(wholeTrialSuccess$trial.success[wholeTrialSuccess$trial.load == 3])
	}	
	#span four trials correct
	if (nrow(subset(wholeTrialSuccess, trial.load == 4)) == 0) {
		#then there were no span size 4 trials, therefore
		span.4.corr <- NA
	} else {
		span.4.corr <- sum(wholeTrialSuccess$trial.success[wholeTrialSuccess$trial.load == 4])
	}	
	#span five trials correct
	if (nrow(subset(wholeTrialSuccess, trial.load == 5)) == 0) {
		#then there were no span size 5 trials, therefore
		span.5.corr <- NA
	} else {
		span.5.corr <- sum(wholeTrialSuccess$trial.success[wholeTrialSuccess$trial.load == 5])
	}
	#span six trials correct
	if (nrow(subset(wholeTrialSuccess, trial.load == 6)) == 0) {
		#then there were no span size 6 trials, therefore
		span.6.corr <- NA
	} else {
		span.6.corr <- sum(wholeTrialSuccess$trial.success[wholeTrialSuccess$trial.load == 6])
	}	
	#span seven trials correct
	if (nrow(subset(wholeTrialSuccess, trial.load == 7)) == 0) {
		#then there were no span size 7 trials, therefore
		span.7.corr <- NA
	} else {
		span.7.corr <- sum(wholeTrialSuccess$trial.success[wholeTrialSuccess$trial.load == 7])
	}	
	#span eight trials correct
	if (nrow(subset(wholeTrialSuccess, trial.load == 8)) == 0) {
		#then there were no span size 8 trials, therefore
		span.8.corr <- NA
	} else {
		span.8.corr <- sum(wholeTrialSuccess$trial.success[wholeTrialSuccess$trial.load == 8])
	}
	#span nine trials correct
	if (nrow(subset(wholeTrialSuccess, trial.load == 9)) == 0) {
		#then there were no span size 9 trials, therefore
		span.9.corr <- NA
	} else {
		span.9.corr <- sum(wholeTrialSuccess$trial.success[wholeTrialSuccess$trial.load == 9])
	}		
	
	#calculate a full-trial-accuracy score
	fta.score <- sum(wholeTrialSuccess$score)
	prop.score <- mean(wholeTrialSuccess$prop.corr)
	median.rt <- median(data.frame$trial.digit_span.durationTime)
	user.name <- data.frame$user.name[1]
	
	p.summary <- c(fta.score, prop.score, number.successes, median.rt, max.span, 
	span.2.corr, span.3.corr, span.4.corr, span.5.corr, span.6.corr, span.7.corr, span.8.corr, 
	span.9.corr)
	
	return(p.summary)
}


##now can use that function on all our data files for digit span
#put all digit span files into one folder and set that folder as working directory

compile_digit_span_data <- function(filestring) {

    raw.data <- read.csv(filestring)
    ps <- unique(raw.data$session.subject.subject.code)
    digit.span.data <- matrix(nrow=length(ps), ncol=13)    

	for (i in 1:length(ps)) {
		#subset data frame with first unique subj code
		sub.raw.data <- subset(raw.data, session.subject.subject.code == ps[i])
				
		tmp.ds.summary <- digit_span_processing(sub.raw.data)
        
		digit.span.data[i, ] <- tmp.ds.summary
	}
	
    digit.span.data <- as.data.frame(digit.span.data)
	digit.span.data$pcode <- ps
	
	ds.names <- c("fta.score", "prop.score", "number.successes", "median.rt", "max.span", 
	"span.2.corr", "span.3.corr", "span.4.corr", "span.5.corr", "span.6.corr", "span.7.corr", 
	"span.8.corr", "span.9.corr", "pcode")
	
	names(digit.span.data) <- ds.names
	
	return(digit.span.data)
}



digit.span.data <- compile_digit_span_data("datafile.csv")

rm(compile_digit_span_data, digit_span_processing)