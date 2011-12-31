#!/usr/bin/perl

# for example:
#  ./3-makeListFromDir.pl May


#$monthDirectory = $ARGV[0];
#$textDirectory = $monthDirectory . "text/";

my $monthName = $ARGV[0];
if ( $monthName eq "" ) {
    print "missing month name argument\n";
    exit;
}

$textDirectory = "data/" . $monthName . "-2011/text/";

#$textDirectory = $ARGV[0];


opendir(DIR, $textDirectory) or die "can't open directory";


#print "$textDirectory \n";

while ( defined( $file=readdir(DIR) ) ) {

    $currentFile = $textDirectory . $file;
    open I, "<$currentFile";

    while ( $word = <I>) {

	$word =~ s/\W/ /g;
	$word = lc $word;

	@lineWords = split / /, $word;


    	foreach $w ( @lineWords ) {

	    $w =~ s/\s//g;
	    $w = lc $w;

	    if ( $w =~ /\w/ ) {
		#print "$w\n";

		#$w =~ s/'/QUOTMARK/g;
		#$w =~ s/\W//g;
		$allWords{$w} = 1;
	    }
	}
    }

    close I;
}


foreach $key ( keys %allWords ) {

    #$key =~ s/QUOTMARK/'/g;
    #print "$key\n";
    #print "$key ";

    push @all, $key;
}

@all = sort @all;

$allWordsFile = $textDirectory . "00-allWords.txt";
open O, ">$allWordsFile";

foreach $item ( @all ) {
    print O "$item \n";
}

close O;
__END__

while ( ($key, $value) == each %allWords) {

    print "$key\n";
}
