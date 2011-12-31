#!/usr/bin/perl -w

use URI::Escape;
use strict;
use warnings;
use LWP::UserAgent;
use HTTP::Request::Common qw(GET);

# ex: ./1-getStoriesWikinews.pl September


# TO DO: need to automatically make month directory

my $monthName = $ARGV[0];
if ( $monthName eq "" ) {
    print "missing month name argument\n";
    exit;
}

#my $monthName =  "September";

my $baseUrl = "http://en.wikinews.org/wiki/";
my $monthDirectory = $baseUrl . "Wikinews:2011/" . $monthName ;
my $localDirectory = "data/" . $monthName . "-2011/complete/";
my $logFile = "log.txt";

# webagent and request objects
my $webAgent = LWP::UserAgent->new;
my $request = GET $monthDirectory;
# request the page
my $answer = $webAgent->request($request);

open LOG, ">>$logFile";

if ( $answer->is_success ) {
    #my $c = $answer->content;

    my @c = split "\n", $answer->content;

    foreach my $contentLine ( @c ) {

	# if the line is a link, like: <li><a href="/wiki/World_leaders_react_to_death_of_Osama_bin_Laden" title="World leaders react to death of Osama bin Laden">World leaders react to death of Osama bin Laden</a></li>
	if ( $contentLine =~ /<li>/ && $contentLine =~ /a href/ ) {

	    # split by quotations to get an array of substrings like: /wiki/World_leaders_react_to_death_of_Osama_bin_Laden
	    my @lineParts = split /\"/, $contentLine;

	    # split by / to get an array of substrings like: World_leaders_react_to_death_of_Osama_bin_Laden
	    #my @substringParts = split /\//, $lineParts[1];
	    # get the story name, which is the third element of @substringParts
	    #my $storyName = $substringParts[2];

	    # remove the first word "wiki"
	    my $storyName = $lineParts[1];
	    $storyName =~ s/\/wiki\///g;


	    print LOG "story name: " . $storyName . "\n";

	    my $longUrl = $baseUrl . $storyName;
	    print LOG "URL: " . $longUrl . "\n";

	    #my $cleanStoryName = $storyName;

	    my $cleanStoryName = uri_escape( $storyName);
	    #$cleanStoryName =~ s/\%..//g;
	    #$cleanStoryName =~ s/\W//g;
	    my $fileName = $localDirectory . $cleanStoryName . ".txt";
	    #$fileName =~ s/\%..//g;
	    #$fileName =~ s/\?//g;
	    #$fileName =~ s/\://g;
	    #$fileName =~ s/\W//g;
	    print LOG "file: " . $fileName . "\n\n";

	    open O, ">$fileName";
	    #print I $longUrl;

	    #request object
	    my $requestPage = GET $longUrl;
            # request the page
	    my $answerPage = $webAgent->request($requestPage);
	    if ( $answerPage->is_success ) {
		print O  $answerPage->content;
	    } else {
		print $answerPage->status_line . "\n";
	    }

	    close O;
	}
    }

} else {
    print $answer->status_line . "\n";
}


close LOG;

__END__

problematic:
http://en.wikinews.org/wiki/Wikinews:2011/May/wiki/%27George_Davis_is_innocent_-_OK%27:_UK_court_partially_vindicates_campaign_after_36_years

http://en.wikinews.org/wiki/%27George_Davis_is_innocent_-_OK%27:_UK_court_partially_vindicates_campaign_after_36_years

#!/usr/bin/perl -w

use strict;
use warnings;
use LWP::UserAgent;
use HTTP::Request::Common qw(GET);


# webagent and request objects
my $webAgent = LWP::UserAgent->new;
my $request = GET 'http://en.wikinews.org/wiki/Wikinews:2011/May';

# request the page
my $answer = $webAgent->request($request);


if ( $answer->is_success ) {
    print $answer->content;
} else {
    print $answer->status_line . "\n";
}
