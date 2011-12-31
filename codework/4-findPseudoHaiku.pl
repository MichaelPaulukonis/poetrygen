#!/usr/bin/perl

# find several lines of codework pseudohaiku 
# ex: ./4-findPseudoHaiku.pl data/July-2011/text/ 5


my $monthName = $ARGV[0];
if ( $monthName eq "" ) {
    print "missing month name argument\n";
    exit;
}

$monthDirectory = "data/" . $monthName . "-2011/text/";

#$monthDirectory = $ARGV[0];


$allWordsFile = $monthDirectory . "/00-allWords.txt";


@all = ();
@seek = ();


#redundant
# make sure month directory is there
#if ( $monthDirectory eq "" ) {
#    print "missing month directory\n";
#    exit;
#}

#make sure user gave number of substrings to generate
$numberOfSubstrings = $ARGV[1];

if ( $numberOfSubstrings == "" ||
     ( $numberOfSubstrings != 5 && $numberOfSubstrings != 7 ) ) {

    print "usage: ./4-findPseudoHaiku.pl data/July-2011/text/ 5\n";
    exit;
}



# make array of words, and shuffle it
sub makeAll(){

    @all = ();
#    @seek = ();
    open I, "<$allWordsFile" || die "Can't open file";


    while ( $word = <I>) {

	$word =~ s/\s//g;
	$l = length $word;


	if ( $word !~ /-/ && $l > 3 ) {

	    push @all, $word;
#	    push @seek, $word;
	}
    }


    $allLength = @all;
    for ($x=0; $x<$allLength; $x++ ) {
	    $index = int(rand($allLength));

	    $temp = $all[$x];
	    $all[$x] = $all[$index];
	    $all[$index] = $temp;
    }

    close I;

}


sub printAll() {
    $allLength = @all;
    for ($x=0; $x<$allLength; $x++ ) {
	print "   $all[$x] \n";
    }
}


sub printStringsSoFar() {
    $ssfLength = @stringsSoFar;
    print "  ";
    for ($x=0; $x<$ssfLength; $x++ ) {
	print "$stringsSoFar[$x]";
	if ($x<$ssfLength-1) {
	    print "][";
	}
    }
    print "\n";
}


# randomly pick a word from the "all" array
sub randomWordFromAll() {

    $allLength = @all;
    $index = int(rand($allLength));
	    
    return $all[$index];
}


# make the @all array
makeAll();

# find a random first word
$firstWord = randomWordFromAll();

@stringsSoFar = ( $firstWord );


while ( $found <= 10 && $attempted < 100000 ) {

    $attempted++;

    $nextWord = randomWordFromAll();

    $finalIndex = @stringsSoFar - 1;
    $finalWord = $stringsSoFar[ $finalIndex ];
    $le = 0 - length($finalWord) + 1;

    #for ( $x=$le; $x<=-1; $x++ ) {
    for ( $x=$le; $x<=-2; $x++ ) {
	$testSubstring = substr $finalWord, $x;
	#print "test: $testSubstring and next: $nextWord\n";

	if ( $nextWord =~ /^$testSubstring/ ) {

	    $y = length($testSubstring);
	    $z = length($nextWord) - $y; 
	    #return $testSubstring;
	    #print "   IS A MATCH\n";
	    #print "   lengths: $y and $z\n";
	    $y1 = 0 - $y;
	    $old = pop(@stringsSoFar);
	    $new2 = substr $testSubstring, $y1;
	    ##print "   $new2\n";
	    $y2 = length($finalWord) + $y1;
	    $new1 = substr $finalWord, 0, $y2;
	    ##print "   $new1\n";
	    push (@stringsSoFar, $new1);
	    push (@stringsSoFar, $new2);

	    $y3 = 0 - (length($nextWord) - $y);
	    $new3 = substr $nextWord, $y3;
	    push (@stringsSoFar, $new3);
	    
	    #printStringsSoFar();

	    if ( @stringsSoFar >= $numberOfSubstrings ) {
		if ( @stringsSoFar == $numberOfSubstrings ) {
		    print "*** SOLUTION FOUND: ";
		    printStringsSoFar();
		}
		$firstWord = randomWordFromAll();
		@stringsSoFar = ( $firstWord );
	    } 
	}
    }

}








__END__



# use the @wordsSoFar array to find the final substring
sub findFinalSubstring {

    # if only one word in wordsSoFar, return the length of that word minus 1
    if ( @wordsSoFar == 1 ) {
	$theWord = $wordsSoFar[0];
	$le = 0 - length($theWord) + 1;
	$finalSubstring = substr $firstWord, $le;

	#print "$finalSubstring\n";


    # if more than one word, use the last two strings to find the final substring
    # ex: if words are "house" and "seen", then the final substring will be "en"
    } else {

	$penultimateIndex = @wordsSoFar - 2;
	$penultimateWord = $wordsSoFar[ $penultimateIndex ];
	#print "$penultimateWord\n";
	$finalIndex = @wordsSoFar - 1;
	$finalWord = $wordsSoFar[ $finalIndex ];
	#print "$finalWord\n";
	$le = 0 - length($penultimateWord) + 1;
	#print "$le\n";
	for ( $x=$le; $x<=-1; $x++ ) {
	    $testSubstring = substr $penultimateWord, $x;
	    #print "$testSubstring\n";
	    if ( $finalWord =~ /^$testSubstring/ ) {

		$y = length($testSubstring);
		# $z = 
		return $testSubstring;
		#print "ECCE\n";
	    }
	}

    }

    return -1;

}

# make the @all array
makeAll();

# find a random first word
$firstWord = randomWordFromAll();

#@wordsSoFar = ( $firstWord );
@wordsSoFar = ( "house", "seen" );


print findFinalSubstring();

__END__

findNextWord( $firstWord );

sub findNextWord {
    my $count = 0;
    my $found = "false";
    while ( $found=="false" && $count<10 ) {
	$count++;
        my $proposedWord = randomWordFromAll();
	print "$proposedWord\n";
	my $finalSubstring = findFinalSubstring();
        #for $x=length($finalSubstring) to 1
           # if $currentWord overlaps at all with $finalSubstring (with something left over)
                # $found = "true";
                # update $wordsUsedSoFar, $stringSoFar, $finalSubstring, $substringCount
                # findNextWord( $wordsUsedSoFar, $stringSoFar, $finalSubstring, $substringCount );
    }
}


__END__

__END__

#@stringsSoFar = ( "" );


# better:
# data (global)
#     - words used so far  (ex: "families escalated")
#     - string so far      (ex: "famili][es][calated")
#     - final substring    (ex: "calated")
#     - substring count    (ex: 3)
#     - substring goal     (ex: 5)
# algorithm:
#    make array of words (which will be randomly accessed, rather than continually shuffled)
#    $firstWord = pickRandomWord();
#    @wordsUsedSoFar = ( $firstWord );
#    @stringsSoFar = ();

#not:
#    $le = 0 - length($firstWord) + 1;
#    $firstWordFinalSubstring = substr $firstWord, le;
#    findNextWord( $firstWord, $firstWord, $firstWordFinalSubstring, 1);
#

sub findNextWord {
#    my ( @wordsUsedSoFar, @stringSoFar, @finalSubstrings, $substringCount ) = @_;
    my $count = 0;
    my $found = "false";
    # while ( $found=="false" || $count<100 ) {
        # $count++;
        # my $currentWord = pickRandomWord();
        # for $x=length($finalSubstring) to 1
           # if $currentWord overlaps at all with $finalSubstring (with something left over)
                # $found = "true";
                # update $wordsUsedSoFar, $stringSoFar, $finalSubstring, $substringCount
                # findNextWord( $wordsUsedSoFar, $stringSoFar, $finalSubstring, $substringCount );
}



# make array of words, and shuffle it
# THIS IS INEFFICIENT - REDO IT
if ( $numberOfSubstrings == 7 ) {

    for ($z=1; $z<=20; $z++) {
	makeAll();
	print findSeven();
    }

} elsif ( $numberOfSubstrings == 5 ) {

    for ($z=1; $z<=20; $z++) {
	makeAll();
	print findFive();
    }
}




sub findNext {

    $current = shift;
    $allDone = 0;

    while ( $allDone == 0 ) {
    #while ( $found == 0 ) {

	$tail = substr $current, $taillength;
	#print "$tail\n";

	foreach $next ( @seek ) {

	    $head = substr $next, 0, $headlength;
	    $diff = (length $current) + $taillength;

	    if ( $head eq $tail && $diff != 0 ) {
		return "$next";
		#return "$current\t$next\t$head\n";
		#print "$current $next $tail $head\n";
		#$found = 1;
		#$foundword = $next;
		#break;
	    }
	}
#	if ( $found == 0 ) {
#	    unshift @all, $current;

    	    if ( $headlength == 2 ) {
		$headlength = 3;
		$taillength = -3;
	    } elsif ( $headlength == 3 ) {
		$headlength = 4;
		$taillength = -4;
	    } elsif ( $headlength == 4 ) {
		$headlength = 2;
		$taillength = -2;
		$allDone = 1;
		return "";
	    }
#	}
    }
    #$allDone = 0;
}


sub findFive {

    $finished = 0;
    $found = 0;
    $iteration = 0;
    $headlength = 2;
    $taillength = -2;
    $foundword = "";
    $first = "";
    $second = "";
    $third = "";

    while ( $second eq "" && $iteration < 200 ) {

	$first = pop(@all);

	$second = findNext( $first );

	if ( $second ne "" ) {

	    while ( $third eq "" && $iteration < 200 ) {
		$third = findNext( $second );
		if ( $third ne "" ) {
    
		    return "$first $second $third\n";
		}
		$iteration++;
	    }
	}

	$iteration++;
    }

}


sub findSeven {

    $finished = 0;
    $found = 0;
    $iteration = 0;
    $headlength = 2;
    $taillength = -2;
    $foundword = "";
    $first = "";
    $second = "";
    $third = "";
    $fourth = "";


    while ( $second eq "" && $iteration < 200 ) {

	$first = pop(@all);
	#print "|" . $current . " " . scalar(@seek) . "|\n";

	$second = findNext( $first );

	if ( $second ne "" ) {

	    while ( $third eq "" && $iteration < 200 ) {
		$third = findNext( $second );

		if ( $third ne "" ) {

		    while ( $fourth eq "" && $iteration < 200 ) {
			$fourth = findNext( $third );

			if ( $fourth ne "" ) {

			    return "$first $second $third $fourth\n";

			}

			$iteration++;
		    }
		}
		$iteration++;
	    }
	}

	$iteration++;
    }
}





__END__
