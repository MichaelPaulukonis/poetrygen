/*

 Copyright 2011 Edde Addad
 Based on Gnoetry by Jon Trowbridge and Eric Elshtain

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


// TODO: it would be nice to remove interactions with the HTML page
// and make it an in/out project
// let some other control push stuff to the form/and vice-versa

// TODO: the only externals refs are for the buttons, I think....

var jGnoetry = function(dbg) {

    var self = this;
    var debug = dbg;

    this.generate = function(templateText, options, corpora) {

        // clear the debug textarea, print initial options
	clearDebug();
	debug( "Options: \n  punctuation handling is: " + options.handlePunctuation, 1 );
	debug( "  starting lines by: " + options.byNewlineOrPunctuation, 1 );

	// get the template text, make an array out of it
	var aTemplateText = makeTemplateArray(templateText, false);
	debug( "\nTemplate Text is: \n" +  templateText, 1 );

	debug( "Generating Array of Words...", 1 );

	// TODO: rename all of this, and come up with a structure to pass in/back data?
        var tInfo = processTemplate(aTemplateText, templateText, corpora, options);
	var aGeneratedWords = tInfo.aGeneratedWords;
	var aGeneratedWordsStatic = tInfo.aGeneratedWordsStatic;
	aTemplateText = tInfo.aTemplateText;
	templateText = tInfo.templateText;

        // print out the generated words array
	debug( "\nGenerated Words:", 1 );
	for ( var i=0; i < aGeneratedWords.length; i++ ) {
	    var generatedWord = aGeneratedWords[i];
	    debug( "    Word " + i + ": " + generatedWord, 1 );
	}


	// print out the static words array
	debug( "\nStatic Generated Words:", 1 );
	for ( var j = 0; j < aGeneratedWordsStatic.length; j++ ) {
	    var generatedWordS = aGeneratedWordsStatic[j];
	    debug( "    Word Static " + j + ": " + generatedWordS, 1 );
	}

	// TODO: better structure to pass in
	// you see how most of this is part of tInfo?!? 
	// !!!
	var gnoe = getOutput(aTemplateText, aGeneratedWords, aGeneratedWordsStatic, corpora, options);

	var output = {
	    displayText: editStringPunctuationOutput(gnoe.outputString),
	    displayEditor: gnoe.editorString,
	    template: templateText
	};

	return output;

    };

    var getOutput = function(aTemplateText, aGeneratedWords, aGeneratedWordsStatic, corpora, options) {

	// number of syllables counted in generated words accepted so far, minus syllables counted in template
	var syllableBalance = 0;

	var historyToPrint = " ";

	// output in terms of final text, and html editor
	var outputString = "";
	var editorString = "";

	// whether the previous token was a punctuation or newline mark
	// (the poem begins with an assumed punctuation and newline)
	var isFollowingPunctuation = true;
	var isFollowingNewline = true;


	// index of array in generated words, representing words accepted so far
	var wordIndex = -1;


	// * * * * * * * * * * * * * * * *
	// FIT THE ARRAY OF WORDS TO THE TEMPLATE

	debug( " ", 1 );
	for ( var i=0; i < aTemplateText.length; i++ ) {

	    var templateToken = aTemplateText[i];
	    debug( "Template Token " + i + ": " + templateToken, 1 );

	    if ( templateToken == "[s]" ) {
		syllableBalance--;

		// accept generated words to change syllable balance
		// make a loop count in case only punctuation being generated - don't hang browser
		var loopCount = 0;
		while ( syllableBalance < 0  && loopCount < 5 ) {

		    // advance wordIndex, identify word there
		    // make sure there is a word there, if not, generate one
		    wordIndex++;
		    var tempWord = "";
		    if ( aGeneratedWords[wordIndex] != null ) {
			tempWord = aGeneratedWords[wordIndex];
		    } else {
			var w = wordIndex-1;
			tempWord = getAWord(aGeneratedWords[w].toLowerCase(), corpora );
			aGeneratedWords.push( tempWord.toLowerCase() );
			aGeneratedWordsStatic.push(false);
		    }

		    // find how many syllables that word has
		    var tempWordSyllCount = countSyllables(tempWord, syllableExceptionsAlgorithm);

		    // re-calculate syllable balance
		    syllableBalance += tempWordSyllCount;
		    loopCount++;

		    debug( "    After adding word: " + tempWord + ", Syllable Balance is " + syllableBalance, 2 );


		    // add word for printout and editor buffers
		    var wordToPrint = tempWord;

		    // if capitalizing first words in sentence, capitalize
		    if ( options.capitalize.customSentence == true && isFollowingPunctuation == true ) {
			wordToPrint = wordToPrint.charAt(0).toUpperCase() + wordToPrint.substring(1);
		    }

		    // if capitalizing first words after a newline, capitalize
		    if ( options.capitalize.customLine == true && isFollowingNewline == true ) {
			wordToPrint = wordToPrint.charAt(0).toUpperCase() + wordToPrint.substring(1);
		    }

		    // if capitalizing "I", capitalize the word to print
		    if ( options.capitalize.customI == true ) {
			if ( wordToPrint == "i" || wordToPrint == "i'll" || wordToPrint == "i'm" || wordToPrint == "i'd" || wordToPrint == "i've") {
			    wordToPrint = wordToPrint.replace("i", "I");
			}
		    }

		    outputString = outputString + " " + wordToPrint;

		    if ( aGeneratedWordsStatic[ wordIndex ] == false ) {
			options.thisWordSelectedBegin = "startSelected";
		    } else if ( aGeneratedWordsStatic[ wordIndex ] == true ) {
			options.thisWordSelectedBegin = "startNotSelected";
		    }
		    // TODO: what's the namespace for appendButton ????
		    editorString = appendButton(editorString, wordToPrint, wordIndex, options.areWordsSelectedBegin, options.thisWordSelectedBegin, options.changeSelectionEffect);

		    // see if next word will be following punctuation
		    isFollowingPunctuation = isEndPunctuation(tempWord);
		    isFollowingNewline = false;
		}


	    } else if ( templateToken == "[n]" ) {

		// make sure the next line doesn't start with punctuation
		var nextWord = aGeneratedWords[wordIndex+1];
		if ( isPunctuation(nextWord) ) {
		    wordIndex++;

		    //wordToPrint = aGeneratedWords[wordIndex];
		    // make sure generated words array is long enough
		    if ( aGeneratedWords[wordIndex] != null ) {
			wordToPrint = aGeneratedWords[wordIndex];
		    } else {
			var v = wordIndex-1;
			wordToPrint = getAWord(aGeneratedWords[v].toLowerCase(), corpora );
			aGeneratedWords.push( tempWord.toLowerCase() );
			aGeneratedWordsStatic.push(false);
		    }


		    outputString = outputString + " " + wordToPrint;

		    if ( aGeneratedWordsStatic[ wordIndex ] == false ) {
			options.thisWordSelectedBegin = "startSelected";
		    } else if ( aGeneratedWordsStatic[ wordIndex ] == true ) {
			options.thisWordSelectedBegin = "startNotSelected";
		    }
		    editorString = appendButton(editorString, wordToPrint, wordIndex, options.areWordsSelectedBegin, options.thisWordSelectedBegin, options.changeSelectionEffect);
		    isFollowingNewline = true;
		}


		outputString += "</br>";
		editorString += "</br></br>";

		isFollowingNewline = true;

		syllableBalance = 0;
		debug( "    Syllable Balance is " + syllableBalance, 2 );

	    }

	}



	// * * * * * * * * * * * * * * * *
	// PRODUCE OUTPUT

	if ( options.appendToPoem != "appendNothing" && ! isFollowingPunctuation ) {
	    wordIndex++;
	    var wordToPrint;
	    if ( options.appendToPoem == "appendPeriod" ) {
		wordToPrint = ".";
		//aGeneratedWords.splice(wordIndex, 0, ".");
	    } else if ( options.appendToPoem == "appendQuestion" ) {
		wordToPrint = "?";
		//aGeneratedWords.splice(wordIndex, 0, "?");
	    } else if ( options.appendToPoem == "appendExclamation" ) {
		//aGeneratedWords.splice(wordIndex, 0, "!");
		wordToPrint = "!";
	    }
	    outputString = outputString + " " + wordToPrint;
	    //editorString = editorString + "<input type='button' style='background-color:#FF9999' value='" + editStringRemoveQuotations(wordToPrint) + "' onmouseover='javascript:switchColor(this)'>";
	    if ( aGeneratedWordsStatic[ wordIndex ] == false ) {
		options.thisWordSelectedBegin = "startSelected";
	    } else if ( aGeneratedWordsStatic[ wordIndex ] == true ) {
		options.thisWordSelectedBegin = "startNotSelected";
	    }
	    editorString = appendButton(editorString, wordToPrint, wordIndex, options.areWordsSelectedBegin, options.thisWordSelectedBegin, options.changeSelectionEffect);
	}

	debug( "", 1 );
	debug( "editor output is: " + editorString, 1 );


	var gnoe = { outputString: outputString,
		     editorString: editorString };

	return gnoe;

    };



    // dictionary of exceptions to syllable algorithm
    // see notes @ https://gnoetrydaily.wordpress.com/2011/12/27/presenting-jgnoetry/#comment-952
    var syllableExceptionsAlgorithm = { "people": 2, "our": 2, "israel": 3, "little": 2, "mr": 2, "being": 2, "saying": 2, "therefore": 2, "called": 1, "p": 1, "mrs": 2, "something": 2, "themselves": 2, "going": 2, "fire": 2, "seemed": 1, "looked": 1, "times": 1, "turned": 1, "c": 1, "used": 1, "m": 1, "l": 1, "dorothea": 4, "asked": 1, "able": 2, "answered": 2, "s": 1, "sometimes": 2, "possible": 3, "everything": 3, "received": 2, "passed": 1, "surely": 2, "desire": 3, "trouble": 2, "doing": 2, "makes": 1, "empire": 3, "business": 2, "beyond": 2, "comes": 1, "several": 2, "battle": 2, "table": 2, "filled": 1, "hour": 2, "seeing": 2, "returned": 2, "states": 1, "didn't": 2, "followed": 2, "delivered": 3, "temple": 2, "interest": 2, "entered": 2, "appeared": 2, "opened": 2, "blessed": 1, "single": 2, "happened": 2, "everybody": 4, "idea": 3, "gathered": 2, "various": 3, "evening": 2, "walked": 1, "james": 1, "there's": 1, "dr": 1, "ones": 1, "b": 1, "loved": 1, "considered": 3, "tabernacle": 4, "killed": 1, "lived": 1, "names": 1, "raised": 1, "believed": 2, "learned": 1, "lives": 1, "hours": 2, "st": 1, "spiritual": 4, "d": 1, "miles": 1, "v": 1, "example": 3, "offered": 2, "society": 4, "t": 1, "covered": 2, "stones": 1, "moved": 1, "prepared": 2, "changed": 1, "likely": 2, "impossible": 4, "g": 1, "tongue": 1, "gates": 1, "experience": 4, "caused": 1, "trying": 2, "middle": 2, "usual": 3, "likewise": 2, "simple": 2, "quiet": 2, "wives": 1, "terrible": 3, "available": 4, "remained": 2, "liked": 1, "noble": 2, "curious": 3, "clothes": 1, "described": 2, "gives": 1, "philistines": 3, "formed": 1, "wished": 1, "allowed": 2, "joshua": 3, "uncle": 2, "especially": 3, "discovered": 3, "takes": 1, "statement": 2, "period": 3, "mentioned": 2, "one's": 1, "lying": 2, "saved": 1, "ashamed": 2, "merely": 2, "minutes": 2, "reached": 1, "bones": 1, "named": 1, "removed": 2, "placed": 1, "situation": 4, "beloved": 2, "stopped": 1, "served": 1, "barbarians": 4, "yourselves": 2, "besides": 2, "possessed": 2, "obtained": 2, "media": 3, "purple": 2, "cattle": 2, "established": 3, "becomes": 2, "determined": 3, "reigned": 1, "doesn't": 2, "whatsoever": 4, "f": 1, "obliged": 2, "tribes": 1, "remembered": 3, "pleased": 1, "sides": 1, "couldn't": 2, "suffered": 2, "wouldn't": 2, "rules": 1, "produced": 2, "charles": 1, "whosoever": 4, "serious": 3, "medea": 3, "created": 3, "increased": 2, "violence": 3, "finished": 2, "ores": 1, "lion": 2, "sanctuary": 4, "actually": 4, "dion": 2, "scattered": 2, "engaged": 2, "title": 2, "talked": 1, "ideas": 3, "armed": 1, "fixed": 1, "preserved": 2, "anyone": 3, "maybe": 2, "imperial": 4, "supposed": 2, "completely": 3, "x": 1, "science": 2, "o'brien": 3, "gentle": 2, "individual": 5, "leaves": 1, "safety": 2, "moab": 2, "double": 2, "somewhat": 2, "zion": 2, "observed": 2, "poured": 1, "movement": 2, "worked": 1, "ruin": 2, "promised": 2, "usually": 4, "showed": 1, "occurred": 2, "superior": 4, "chariots": 3, "syria": 3, "notes": 1, "creative": 3, "principle": 3, "isn't": 2, "burned": 1, "somebody": 3, "statutes": 2, "bowed": 1, "refused": 2, "royal": 2, "expressed": 2, "continually": 5, "pictures": 2, "escaped": 2, "material": 4, "considerable": 5, "centre": 2, "useful": 2, "radio": 3, "touched": 1, "feared": 1, "stretched": 1, "declared": 2, "proposed": 2, "forced": 1, "carefully": 3, "influence": 3, "earlier": 3, "gradually": 4, "glorious": 3, "reality": 4, "measures": 2, "lines": 1, "aged": 1, "surprised": 2, "distinguished": 3, "consumed": 2, "proved": 1, "jeremiah": 4, "closed": 1, "miserable": 4, "gained": 1, "arrived": 2, "chariot": 3, "hopes": 1, "somehow": 2, "marked": 1, "n": 1, "ordered": 2, "unable": 3, "joined": 1, "sovereign": 2, "numbered": 2, "etc": 4, "samaria": 4, "published": 2, "playing": 2, "gentiles": 2, "wasn't": 2, "mixed": 1, "intellectual": 5, "cruel": 2, "immediate": 4, "scarcely": 2, "explained": 2, "r": 1, "sinned": 1, "struggle": 2, "ph": 2, "announced": 2, "failed": 1, "careful": 2, "require": 3, "creatures": 2, "concerned": 2, "capable": 3, "charged": 1, "laughed": 1, "obvious": 3, "dropped": 1, "despised": 2, "dying": 2, "measured": 2, "accustomed": 3, "violent": 3, "y'all": 1, "somewhere": 2, "household": 2, "colored": 2, "watched": 1, "fulfilled": 2, "helped": 1, "assyria": 4, "based": 1, "humble": 2, "released": 2, "marble": 2, "noticed": 2, "clothed": 1, "plague": 1, "carrying": 3, "contained": 2, "dressed": 1, "claimed": 1, "involved": 2, "actual": 3, "introduced": 3, "advanced": 2, "judged": 1, "area": 3, "oppressed": 2, "honorable": 4, "flying": 2, "valuable": 3, "revealed": 2, "crying": 2, "slaves": 1, "create": 2, "washed": 1, "vague": 1, "trial": 2, "confirmed": 2, "exposed": 2, "ceased": 1, "entire": 3, "performed": 2, "mysterious": 4, "cursed": 1, "wondered": 2, "couple": 2, "previously": 4, "resolved": 2, "w": 3, "restored": 2, "associated": 5, "triumph": 2, "extremely": 3, "convinced": 2, "visible": 3, "accomplished": 3, "remarkable": 4, "attacked": 2, "pressed": 1, "seized": 1, "curiosity": 5, "previous": 3, "inclined": 2, "interests": 2, "sexual": 3, "quietly": 3, "everywhere": 3, "treasures": 2, "appropriate": 4, "namely": 2, "figures": 2, "machines": 2, "farewell": 2, "jeroboam": 4, "graduate": 3, "acknowledged": 3, "interested": 3, "lately": 2, "probable": 3, "reduced": 2, "whereby": 2, "pitched": 1, "interesting": 3, "useless": 2, "agreeable": 4, "easier": 3, "informed": 2, "accused": 2, "pleasures": 2, "remarked": 2, "assumed": 2, "attached": 2, "desperate": 2, "games": 1, "exclaimed": 2, "astonished": 3, "audience": 3, "careless": 2, "horsemen": 2, "healed": 1, "waves": 1, "perpetual": 4, "obviously": 4, "worshipped": 2, "lonely": 2, "features": 2, "composed": 2, "condemned": 2, "louis": 2, "creativity": 5, "article": 3, "ruins": 2, "perceived": 2, "rites": 1, "scientific": 4, "trembling": 3, "grateful": 2, "lovely": 2, "q": 1, "compared": 2, "devour": 3, "spoiled": 1, "individuals": 5, "h": 1, "deceived": 2, "managed": 2, "paused": 1, "loves": 1, "confined": 2, "derived": 2, "likeness": 2, "poet": 2, "genuine": 3, "assured": 2, "hoped": 1, "inferior": 4, "perished": 2, "imagined": 3, "listened": 2, "anxiety": 4, "feeble": 2, "closely": 2, "idle": 2, "designed": 2, "reserved": 2, "enable": 3, "maintained": 2, "compelled": 2, "association": 5, "preached": 1, "urged": 1, "likes": 1, "atonement": 3, "conceived": 2, "abandoned": 3, "hadn't": 2, "sacrificed": 3, "prevailed": 2, "disposed": 2, "wandered": 2, "doings": 2, "twentieth": 3, "paying": 2, "approved": 2, "rendered": 2, "describes": 2, "poetry": 3, "prism": 2, "addressed": 2, "punished": 2, "injured": 2, "barbarian": 4, "circle": 2, "movements": 2, "horrible": 3, "pushed": 1, "iago": 3, "recognized": 3, "declined": 2, "vanished": 2, "christianity": 5, "lively": 2, "dared": 1, "formidable": 4, "deity": 3, "obedience": 4, "pronounced": 2, "thereby": 2, "captives": 2, "deserved": 2, "stirred": 1, "beings": 2, "ts": 2, "uttered": 2, "sealed": 1};



    // * * * * * * * * * * * * * * * *
    // CHAINED N-GRAM FUNCTIONS

    var makeWordsArray = function(numberOfWords, byNewlineOrPunctuation, corpora ) {

	// the array that will be returned
	var toReturn = new Array();

	// the n-gram history
	var history = " ";

	// whether it is the first word in a sentence
	var isFirstWord = true;

	for ( var i=0; i<numberOfWords; i++ ) {

	    debug( " ",  2 );
	    debug( "Generating Word number: " + i + ": ",  2 );

	    if ( isFirstWord == true ) {

		debug("  is a first word",  2);
		debug("  finding a word",  2);
		debug("  - - -",  2);

		history = getAFirstWord(isFirstWord, byNewlineOrPunctuation, corpora );

		debug("  - - -",  2);
		debug("  found a word: " + history,  2 );

		isFirstWord = false;

		// add to output array
		toReturn.push( history);

	    } else {

		debug("  is not a first word",  2);
		debug("  finding a word",  2);
		debug("  - - -",  2);

		history = getAWord(history, corpora );

		// add to output array
		toReturn.push( history );

		debug("  - - -",  2);
		debug("  found a word: " + history,  2 );

	    }

	}

	return toReturn;

    };


    // TO DO: don't need the "isFirstWord" value here?
    // get the first word of a sentence being generated
    var getAFirstWord = function(isFirstWord, byNewlineOrPunctuation, corpora ) {

	// identify the corpus to use
	var corpusText = "";
	// generate a random number from 1 to 100 (since weights are a percentage)
	var randomWeight = Math.floor(Math.random()*100);
	// for each text, get its weight, subtract it from the random number
	// if the total is 0 or less, use that text
	for( var i=0; i < corpora.texts.length; i++ ) {
	    debug( "  randomWeight is: " + randomWeight + " and corpora.weights[" + i + "] is: " +  corpora.weights[i],  2 );
	    randomWeight = randomWeight - corpora.weights[i];
	    if ( randomWeight <= 0 ){
		corpusText = corpora.texts[i];
		debug( "  decided on corpus " + i,  2 );
		break;
	    }
	}


	// find a random location in the corpusText string
	var randomIndex = Math.floor(Math.random()*corpusText.length);

	// if we are finding by punctuation
	if ( byNewlineOrPunctuation != "newline" ) {

	    // if is byPunctuation - identify the first punctuation after the random location
	    var punctuationFound = findFollowingPunctuation( corpusText, randomIndex);

	    if ( punctuationFound != "" ) {
		debug( "  closest punctuation found is: " + punctuationFound,  2 );
		// find the first word after the punctuation
		return findFollowingWord(corpusText, punctuationFound, randomIndex);
	    }
	    debug( "  not found a punctuation",  2 );
	}

	// if we get to this point, we are finding by newline
	// either by choice or because corpus has no punctuation
	debug( "  finding line following newline",  2);
	return findFollowingWord(corpusText, "\n", randomIndex);
    };



    var hasNonTemplateWord = function( aTemplateText ) {

	for ( var i=0; i<aTemplateText.length; i++ ) {
	    if ( aTemplateText[i] != "[s]" && aTemplateText[i] != "[n]" ) {
		return true;
	    }
	}

	return false;
    };


    // whether a given word is an end punctuation or not
    var isEndPunctuation = function( word ) {

	if ( word == "." || word == "?" || word == "!" ) {
	    return true;
	}
	return false;
    };


    // whether a given word is an end punctuation or not
    var isPunctuation = function( word ) {

	if ( word == "." || word == "?" || word == "!" || word == "," || word == ":" || word == ";" || word == "--" || word == "\"" ) {  // "
	    return true;
	}
	return false;
    };


    // from the starting index to the end of the corpus text, find the nearest punctuation
    // returns "" if nothing found
    var findFollowingPunctuation = function( corpusText, startingIndex) {

	var punctuationFound = "";
	var nextPunctuationIndex = -1;
	var n;
	var punctuation = new Array(".", "?", "!");

	// look through the punctuations, identifying which comes next
	for ( x in punctuation ) {
	    n = corpusText.indexOf( punctuation[x], startingIndex );
	    debug( "    index of " + punctuation[x] + " is " + n,  2 );

	    // if the punctuation is found... (and it's not the last character)
	    if ( n != -1 && n != corpusText.length -2) {
		// if a 'lowest index so far' has been found, and n is lower than it
		// then set the 'lowest index so far' to n
		// if 'lowest index so far' has not yet been found, set it to n
		if ( nextPunctuationIndex != -1 && n < nextPunctuationIndex ) {
		    nextPunctuationIndex = n;
		    punctuationFound = corpusText.charAt(nextPunctuationIndex);
		} else if (nextPunctuationIndex == -1 ) {
		    nextPunctuationIndex = n;
		    punctuationFound = corpusText.charAt(nextPunctuationIndex);
		}
	    }
	    debug( "    corpusText.length is: " + corpusText.length,  2 );
	    debug( "    nextPunctuationIndex is: " + n,  2 );
	    debug( "    punctuationFound is: " + punctuationFound,  2 );
	}

	// if nothing found, look from beginning of text
	if (nextPunctuationIndex == -1 ) {
	    // look through the punctuations, identifying which comes next
	    for ( x in punctuation ) {
		n = corpusText.indexOf( punctuation[x] );
		debug( "    index of " + punctuation[x] + " is " + n ,  2);

		// if the punctuation is found...
		//if ( n != -1 ) {
		// if the punctuation is found... (and it's not the last character)
		if ( n != -1 && n != corpusText.length -2) {
		    // if a 'lowest index so far' has been found, and n is lower than it
		    // then set the 'lowest index so far' to n
		    // if 'lowest index so far' has not yet been found, set it to n
		    if ( nextPunctuationIndex != -1 && n < nextPunctuationIndex ) {
			nextPunctuationIndex = n;
			punctuationFound = corpusText.charAt(nextPunctuationIndex);
		    } else if (nextPunctuationIndex == -1 ) {
			nextPunctuationIndex = n;
			punctuationFound = corpusText.charAt(nextPunctuationIndex);
		    }
		}
		debug( "    nextPunctuationIndex is: " + n ,  2);
		debug( "    punctuationFound is: " + punctuationFound,  2 );
	    }
	}

	return punctuationFound;
    };



    // given a corpusText string and a previous word,
    // find the next word
    var getAWord = function( history, corpora ) {

	// identify the corpus to use
	var corpusText = "";
	// generate a random number from 1 to 100 (since weights are a percentage)
	var randomWeight = Math.floor(Math.random()*100);
	// for each text, get its weight, subtract it from the random number
	// if the total is 0 or less, use that text
	for( var i = 0; i < corpora.texts.length; i++ ) {
	    debug( "  randomWeight is: " + randomWeight + " and corpora.weights[" + i + "] is: " +  corpora.weights[i],  2 );
	    randomWeight = randomWeight - corpora.weights[i];
	    if ( randomWeight <= 0 ){
		corpusText = corpora.texts[i];
		debug( "  decided on corpus " + i,  2 );
		break;
	    }
	}


	// find a random location in the corpusText string
	var randomIndex = Math.floor(Math.random()*corpusText.length);

	debug( "  randomIndex is: " + randomIndex,  2 );
	debug( "  character in context: " + getCharacterInContext(corpusText, randomIndex),  2 );

	// find the next word after it given the history
	return findFollowingWord(corpusText, history, randomIndex);

    };


    // given a corpusText and an index in it, find the next word
    // (may have to start looking at the beginning)
    var findFollowingWord = function( corpusText, history, startingIndex) {

	// var will be the index of the first character after the history
	var nextIndex = 0;

	// add surrounding spaces so you don't find substrings (ex: "her" in "where")
	// remember: modified corpus has spaces around most punctuation
	// TO DO: (can probably remove conditional after implemeting getAFirstWord)
	if ( history != " " ) {
	    history = " " + history + " ";
	}

	// if you do not find a new word?  (corpus difference, static word, last word in corpus)
	// first, make sure the history word is actually in the corpus
	// if not, set the history word to " "
	if ( corpusText.indexOf( history ) == -1 ) {
	    debug("  history "+history+" not found in corpus, setting to blank",  2);
	    history = " ";
	}

	// Find the first non-space character after the history

	// look for the index that starts the history
	var indexOfHistory = corpusText.indexOf( history, startingIndex );

	// if you haven't found the history text by the end of the corpus,
	// look from the beginning
	if ( indexOfHistory == -1 ) {
	    indexOfHistory = corpusText.indexOf( history );
	}

	// identify the first character after the history
	var indexAfterHistory = indexOfHistory + history.length;

	// advance past spaces and newlines that might be after the history
	// (due to corpus text editing, there will not be more than " \n " )
	if ( corpusText.charAt(indexAfterHistory) == " " ) {
	    indexAfterHistory++;
	}
	if ( corpusText.charAt(indexAfterHistory) == "\n" ) {
	    indexAfterHistory++;
	}
	if ( corpusText.charAt(indexAfterHistory) == " " ) {
	    indexAfterHistory++;
	}
	debug( "  first non-space index after history is: " + indexAfterHistory,  2 );
	debug( "  character in context: " + getCharacterInContext(corpusText, indexAfterHistory),  2 );


	// find the first space after the end of the history
	// (i.e. the space-delimited token following the history text)
	var firstSpaceAfterHistory = corpusText.indexOf( " ", indexAfterHistory );

	// if the history is the last token in the text, search from the beginning.
	if ( firstSpaceAfterHistory == -1 ) {
	    indexOfHistory = corpusText.indexOf( history );
	    indexAfterHistory = indexOfHistory + history.length;
	    // advance past spaces and newlines that might be after the history
	    if ( corpusText.charAt(indexAfterHistory) == " " ) {
		indexAfterHistory++;
	    }
	    if ( corpusText.charAt(indexAfterHistory) == "\n" ) {
		indexAfterHistory++;
	    }
	    if ( corpusText.charAt(indexAfterHistory) == " " ) {
		indexAfterHistory++;
	    }
	    debug( "  RECALCULATED first non-space index after history is: " + indexAfterHistory,  2 );
	    debug( "  character in context: " + getCharacterInContext(corpusText, indexAfterHistory),  2 );
	    firstSpaceAfterHistory = corpusText.indexOf( " ", indexAfterHistory );
	}

	debug( "  first space after history is: " + firstSpaceAfterHistory,  2 );
	debug( "  character in context: " + getCharacterInContext(corpusText, firstSpaceAfterHistory),  2 );

	// if the history is the last token in the text, and the last token is unique, report failure
	if ( firstSpaceAfterHistory == -1 ) {
	    return "";
	}

	// find the first word after the history
	var firstWordAfterHistory = corpusText.substring(indexAfterHistory, firstSpaceAfterHistory);
	debug( "  first word after history is: " + firstWordAfterHistory + "\n" ,  2 );


	return firstWordAfterHistory;
    };


    // currently a very naive way of counting syllables:
    // count how many distinct vowel sequences the word has
    var countSyllables = function( wordToCount, syllableExceptionsAlgorithm) {

	//debug( "*** incoming word: " + wordToCount,  1 );
	//debug( "*** incoming word: " + syllableExceptionsAlgorithm[ wordToCount ],  1 );

	wordToCount = wordToCount.toLowerCase();

	if ( syllableExceptionsAlgorithm.hasOwnProperty(wordToCount) ) {
	    //debug( "*** returning from lookup: " + syllableExceptionsAlgorithm[ wordToCount ],  1 );
	    return syllableExceptionsAlgorithm[ wordToCount ];
	}

	var editedWord = wordToCount;
	if ( editedWord.match(/.*[aeiouy].+[aeiouy].*/) ) {
	    editedWord = editedWord.replace(/e$/g, "");
	}
	editedWord = editedWord.replace(/[aeiouy]+/g, "e");
	//debugOutput( "    edited word: " + editedWord );

	var t = editedWord.split("e").length - 1;
	//debug( "*** returning from algorithm: " +  t,  1 );

	var toReturn = editedWord.split("e").length - 1;
	if ( toReturn < 0 ) {
	    toReturn = 0;
	}

	return toReturn;
    };



    // * * * * * * * * * * * * * * * *
    // STRING EDITING FUNCTIONS


    // get template text, make array out of it
    var makeTemplateArray = function( templateText, keepNewlines ) {

	// remove any trailing newlines (so don't have hanging end punctuation)
	templateText = templateText.replace(/[\n\r]+$/g, " ");
	templateText = templateText.replace(/\n+$/g, " ");

	if ( keepNewlines ) {
	    templateText = templateText.replace(/\r/g, " \r ");
	    templateText = templateText.replace(/\n/g, " \n ");

	    // place spaces around parens
	    templateText = templateText.replace(/\"/g, " \" ");
	    templateText = templateText.replace(/\(/g, " \( ");
	    templateText = templateText.replace(/\)/g, " \) ");
	    templateText = templateText.replace(/\[/g, " \[ ");
	    templateText = templateText.replace(/\]/g, " \] ");
	    templateText = templateText.replace(/\{/g, " \{ ");
	    templateText = templateText.replace(/\}/g, " \} ");

	    // collapse multiple repeated punctuations
	    templateText = templateText.replace(/\.+/g, " . ");
	    templateText = templateText.replace(/\!+/g, " ! ");
	    templateText = templateText.replace(/\?+/g, " ? ");

	    // place spaces around certain punctuation (but not dashes and apostrophes)
	    templateText = templateText.replace(/,/g, " , ");
	    templateText = templateText.replace(/\./g, " \. ");
	    templateText = templateText.replace(/\?/g, " \? ");
	    templateText = templateText.replace(/!/g, " ! ");
	    templateText = templateText.replace(/;/g, " ; ");
	    templateText = templateText.replace(/:/g, " : ");
	    templateText = templateText.replace(/--/g, " -- ");
	} else {
	    templateText = templateText.replace(/\r/g, " ");
	    templateText = templateText.replace(/\n/g, " ");
	}


	templateText = templateText.replace(/ +/g, " ");
	templateText = templateText.replace(/^ /g, "");
	templateText = templateText.replace(/ $/g, "");
	return templateText.split(" ");
    };




    // remove quotations in a string
    var editStringRemoveQuotations = function( inputText ) {

	inputText = inputText.replace(/'/g, "&#39;");
	inputText = inputText.replace(/"/g, "&#34;");  // handle "

	return inputText;
    };

    // TODO: does not appear to be used...
    // remove punctuation and newlines (except apostrophes and single-dashes)
    var editStringRemovePunctuationNewlines = function( inputText ) {

	inputText = inputText.replace(/,/g, "");
	inputText = inputText.replace(/\./g, "");
	inputText = inputText.replace(/\?/g, "");
	inputText = inputText.replace(/!/g, "");
	inputText = inputText.replace(/;/g, "");
	inputText = inputText.replace(/:/g, "");
	inputText = inputText.replace(/--/g, "");
	inputText = inputText.replace(/\r/g, "");
	inputText = inputText.replace(/\n/g, "");

	return inputText;
    };





    // remove spaces before in-sentence punctuation such as : and , and .
    var editStringPunctuationOutput = function( inputText ) {

	// place spaces at beginning and end of corpus
	inputText = " " + inputText + " ";

	// place spaces around certain punctuation (but not dashes and apostrophes)
	inputText = inputText.replace(/ ,/g, ",");
	inputText = inputText.replace(/ \./g, "\.");
	inputText = inputText.replace(/ \?/g, "\? ");
	inputText = inputText.replace(/ !/g, "! ");
	inputText = inputText.replace(/ ;/g, "; ");
	inputText = inputText.replace(/ :/g, ": ");

	return inputText;
    };

    var editString = function( inputText ) {

	inputText = inputText.replace(/"/g, " \" ");

	inputText = inputText.replace(/([^a-zA-Z0-9_ \n\r'])/g, " $1");
	inputText = inputText.replace(/\r/g, " \r ");
	inputText = inputText.replace(/\n/g, " \n ");
	inputText = inputText.replace(/ +/g, " ");
	inputText = inputText.replace(/^ /g, "");
	inputText = inputText.replace(/ $/g, "");

	return inputText;
    };


    // replace newlines with characters
    var replaceNewlines = function( aString ) {

	return aString.replace(/[\n\r]/g, "\\n");

    };


    // given a text string and and index in it, get several characters to either side of it
    // (for debugging purposes)
    var getCharacterInContext = function( aString, anIndex ) {

	var tempString = "";
	var returnString = "";

	tempString = aString.substring(anIndex-8, anIndex);
	returnString += replaceNewlines(tempString);

	returnString += "|" + replaceNewlines( aString.charAt(anIndex) ) + "|";

	tempString = aString.substring(anIndex+1, anIndex+9);
	returnString += replaceNewlines(tempString);
	//returnString += "\n";

	return returnString;
    };

    var processTemplate = function(aTemplateText, templateText, corpora, options) {
        
	// generate a variable-length array of words
	var initialNumberWords = aTemplateText.length * 2;
	var aGeneratedWords = new Array();
	var aGeneratedWordsStatic = new Array();

	// if there are any non-template words in template: generate from that
	//   (i.e. you've pasted in a poem to edit)
	// else if NO word button exists in the editor area, generate from scratch
	//   (i.e. you're starting a new poem)
	// otherwise make list based on whats in the editor area
	//   (i.e. you're re-generating a new version of a poem)

        // 'w0' == first word element
	var oElement = parent.editor.document.getElementById('w0');

	var history = "";

	// TODO: replace hard-coded tokens with refs to token.
	var token = {
	    syllable: "[s]",
	    linebreak: "[n]",
	    hardbreak: "\n"  // the problem is, I don't understand why this token exists.....
	};
        
        // only if non-standard template (ie, text pasted into template)
	if ( hasNonTemplateWord( aTemplateText ) ) {

	    aTemplateText = makeTemplateArray( templateText, true );
	    debug( "\nTemplate Text is: \n" +  templateText, 1 );

	    // figure out the words to use and which to replace
	    for ( var i = 0; i < aTemplateText.length; i++ ) {

		var templateToken = aTemplateText[i];
		if ( templateToken == token.syllable ) {

		    syllableBalance--;
		    var loopCount = 0;
		    while ( syllableBalance < 0  && loopCount < 5 ) {

			if ( i==0 ) {
			    history = getAFirstWord( isFirstWord, options.byNewlineOrPunctuation, corpora );
			} else {
			    history = getAWord(aTemplateText[i], corpora );
			}

			syllableBalance += countSyllables( history, syllableExceptionsAlgorithm);
			aGeneratedWords.push( history.toLowerCase() );

			loopCount++;
		    }

		} else if ( templateToken == "[n]" ) {
		    // ignore?
		} else if ( templateToken == "\n" ) {
		    // ignore?
		} else {
		    history = templateToken.toLowerCase();
		    aGeneratedWords.push( history );
		}
	    }


	    // figure out what the new template is

	    // make template text array, keeping newlines
	    var newTemplate = "";

	    for ( var i = 0; i < aTemplateText.length; i++ ) {

		var templateToken = aTemplateText[i];

		if ( templateToken == token.syllable ) {
		    newTemplate += templateToken + " ";
		} else if ( templateToken == "[n]" ) {
		    newTemplate += templateToken + "\n";
		} else if ( templateToken == "\n" ) {
		    newTemplate += "[n]\n";
		} else {
		    var x = countSyllables( templateToken, syllableExceptionsAlgorithm);
		    for ( var j=0; j<x; j++ ) {
			newTemplate += token.syllable +  " "; // add a space for human readability
		    }
		    debug(" word " + templateToken + " has " + x + " syllables ", 1 );

		}
	    }

	    templateText = newTemplate;

	    aTemplateText = makeTemplateArray(templateText, false);
	    debug( "\nTemplate Text is: \n" +  templateText, 1 );

	} else if  ( oElement == null ) { // first time we've run the program -- there is no active button-text area

	    aGeneratedWords = makeWordsArray(initialNumberWords, options.byNewlineOrPunctuation, corpora );

	} else {
	    // TO DO: move this to its own function once stabilized
	    var i=0;

	    while ( oElement != null ) {

		var editorWord = oElement.value;

		// TODO: this is INTIMATELY tied to a particular display model
  	        //       refactor so display model has no impace -- but the funcationality remains the same
		// if it is selected for regeneration, find a new word
		// otherwise just add the word
		if ( oElement.style.backgroundColor != "transparent" ) {
		    aGeneratedWordsStatic.push(false);
		    // if its the first word, find a new starting word
		    // otherwise find a word based on the previous word
		    if ( i==0 ) {
			editorWord = getAFirstWord( true, options.byNewlineOrPunctuation, corpora );
		    } else {
			editorWord = getAWord(aGeneratedWords[i-1], corpora );
		    }
		} else {
		    aGeneratedWordsStatic.push(true);
		}

		aGeneratedWords.push( editorWord.toLowerCase() );


		i++;
		var currentId = "w" + i;

		oElement = parent.editor.document.getElementById( currentId );
	    }
	}

        var tInfo = {
            aTemplateText: aTemplateText,
            templateText: templateText,
            aGeneratedWords: aGeneratedWords,
	    aGeneratedWordsStatic: aGeneratedWordsStatic

        };

        return tInfo;
        

    };


};