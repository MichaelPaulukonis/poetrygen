#!/usr/bin/perl

# for example:
#  ./3-makeTextFromDir.pl May

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

$allWordsFile = $textDirectory . "00-allText.txt";
open O, ">$allWordsFile";


while ( defined( $file=readdir(DIR) ) ) {

    if ( $file ne "00-allWords.txt" && $file ne "00-allText.txt") {
	$currentFile = $textDirectory . $file;
	open I, "<$currentFile";

	while ( $line = <I>) {

	    print O $line;
	}

	close I;
    }
}


close O;

__END__

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
