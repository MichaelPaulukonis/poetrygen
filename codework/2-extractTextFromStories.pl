#!/usr/bin/perl


# for example:
#   ./2-extractTextFromStories.pl May


# TO DO: make sure month directory is really there?


my $monthName = $ARGV[0];
if ( $monthName eq "" ) {
    print "missing month name argument\n";
    exit;
}

$monthDirectory = "data/" . $monthName . "-2011/";


$storiesDirectory = $monthDirectory . "complete/";
$textDirectory = $monthDirectory . "text/";


opendir(DIR, $storiesDirectory) or die "can't open directory";


while ( defined( $fileName=readdir(DIR) ) ) {

    $storyFile = $storiesDirectory . $fileName;
    $textFile = $textDirectory . $fileName;
    $endReached = 0;

    #print "$storyFile \n";
    #print "$textFile \n";

    open I, "<$storyFile";
    open O, ">$textFile";

    while ( $line = <I> ) {

	if ( $line =~ /Have an opinion on this story/) {
	    $endReached = 1;
	}

	if ( $line =~ /<p>/ && $line !~ /publishDate/ && $endReached == 0) {

	    $line =~ s/<.*?>//g;
	    #$line =~ s/\W/ /g;
	    #$line = lc $line;

	    print O $line;
	}
    }

    close O;
    close I;
} 



__END__

$file = $ARGV[0];

if ( $file eq "" ) {
    exit;
}

open I, $file;

#open I, "nineJoySuicidal.txt";
#open I, "declineAmericanEmpireNews.txt";
#open I, "declineEastlandEmpire.txt";
#open I, "WikinewsLatestNews-2.txt";
#open I, "closed-class-words.txt";

while ( $word = <I>) {

    @lineWords = split / /, $word;


    foreach $w ( @lineWords ) {

	$w =~ s/\s//g;
	$w = lc $w;

	if ( $w =~ /\w/ ) {
	    #print "$w\n";

	    $w =~ s/'/QUOTMARK/g;
	    $w =~ s/\W//g;
	    $allWords{$w} = 1;

	}
    }


}


foreach $key ( keys %allWords ) {

    $key =~ s/QUOTMARK/'/g;
    #print "$key\n";
    #print "$key ";

    push @all, $key;
}

@all = sort @all;


foreach $item ( @all ) {
    print "$item \n";
}

__END__

while ( ($key, $value) == each %allWords) {

    print "$key\n";
}
