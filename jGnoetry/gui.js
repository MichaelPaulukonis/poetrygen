// Andy Langton's show/hide/mini-accordion - updated 18/03/2009
// Latest version @ http://andylangton.co.uk/jquery-show-hide
// http://andylangton.co.uk/articles/javascript/jquery-show-hide-multiple-elements/

// this tells jquery to run the function below once the DOM is ready
$(document).ready(
    function() {

        setVisibility('templateSpan', false);
        setVisibility('statusSpan', false);
        setVisibility('corpusSpan', false);
        setVisibility('optionsSpan', false);

        var g = new gui();
        g.showHide();

        g.setCorpusTexts();

        $('#generationButton').click(g.callGenerate);

        // since these are set as hrefs, we can't dump params in there right now...
        // this is duplicating the original code, just removing from the html
        // $('#selectAll').click(function() { setSelectedAll(); });
        $('#selectAll').click(setSelectedAll);
        $('#unselectAll').click(setSelectedNone);
        $('#exportText').click(exportText);
        $('#exportDebug').click(exportDebug);

        $('#baseForm').bind('change', function() { setBaseForm(this.form.baseForm); } );

        $('#toggleCorpora').click(toggleCorpus);
        $('#toggleTemplate').click(toggleTemplate);
        $('#toggleStatus').click(toggleStatus);
        $('#toggleOptions').click(toggleOptions);

        $('#clearTemplate').click(function() { clearTextarea('templateText'); });
        $('#clearCorpus1').click(function() { clearCorpus('corpus1'); });
        $('#clearCorpus2').click(function() { clearCorpus('corpus2'); });
        $('#clearCorpus3').click(function() { clearCorpus('corpus3'); });

        $('[id!=weightCorpus]').bind('change', calculateWeights);

        // so, what this adds has hard-coded values in it...
        $('#addCorpus').click(function() { addCorpus(parent.control.document); });
    }

);

var gui = function() {

    var self = this;
    var debug;

    this.callGenerate = function() {

        var control = parent.control.document;

        var options = self.getOptions(control);

        // capture statusVerbosity, and never [for scoped-functions] refer to it again
        debug = function(msg, level) {
            debugOutput(msg, options.statusVerbosity, level);
        };

        var templateText = control.getElementById('templateText').value;

        var corpora = self.getCorpora(options.capitalize.method, options.handlePunctuation);

        // show the editing options
        control.getElementById('editingSpan').style.visibility = 'visible';
        control.getElementById('generationButton').value = 'Re-Generate';

        // ideally, debug [wrapper] is scoped to ALL of the functions called by generate, as well
        // this requires encapsulating them all....
        var jg = new jGnoetry(debug);
        var existingText = self.getExistingText();
        // TODO: ugh. that's a lot of ugly parameters
        var output = jg.generate(templateText, options, corpora, existingText);

        // dump to display
        parent.display.document.getElementById('displayText').innerHTML = output.displayText;
        parent.editor.document.getElementById('jGnoetryTitle').innerHTML = '';
        parent.editor.document.getElementById('displayEditor').innerHTML = output.displayEditor;
        control.getElementById('templateText').value = output.template;


    };

    // TODO: CLEANUP!
    this.getOptions = function(document) {

        var opunct = document.getElementById('optionsPunctuation');
        var opsl = document.getElementById('optionsStartlines');
        var options = {
            // parameter: whether to remove parentheticals (or place spaces around them)
            handlePunctuation: opunct.options[opunct.selectedIndex].value,
            // parameter: how we should start new lines: by those words following newlines, or following punctuation
            byNewlineOrPunctuation:  opsl.options[opsl.selectedIndex].value
        };

        // parameters: how we should capitalize
        var capitalizeMethod = '';
        var capitalizeCustomSentence = false;
        var capitalizeCustomLine = false;
        var capitalizeCustomI = false;

        if ( document.getElementById('capitalizeAsCorpus').checked == true ) {
            capitalizeMethod = 'capitalizeAsCorpus';
        } else  if ( document.getElementById('capitalizeNone').checked == true ) {
            capitalizeMethod = 'capitalizeNone';
        } else  if ( document.getElementById('capitalizeCustom').checked == true ) {

            capitalizeMethod = 'capitalizeCustom';

            capitalizeCustomSentence = document.getElementById('capitalizeCustomSentence').checked;
            capitalizeCustomLine = document.getElementById('capitalizeCustomLine').checked;
            capitalizeCustomI = document.getElementById('capitalizeCustomI').checked;

        }

        options.capitalize = {
            method: capitalizeMethod,
            customSentence: capitalizeCustomSentence,
            customLine: capitalizeCustomLine,
            customI: capitalizeCustomI
        };

        // parameter: what you should append to end of poem if there is no punctuation there already:
        // appendPeriod, appendQuestion, appendExclamation, appendNothing
        var oa = document.getElementById('optionsAppend');
        var appendToPoem = oa.options[oa.selectedIndex].value;

        // parameter: whether editor should begin with words selected or not
        var oss = document.getElementById('optionsStartSelected');
        var areWordsSelectedBegin = oss.options[oss.selectedIndex].value;
        var thisWordSelectedBegin = areWordsSelectedBegin ;

        // parameter: mouseover effect
        var ocs = document.getElementById('optionsChangeSelected');
        var changeSelectionEffect = ocs.options[ocs.selectedIndex].value;

        options.appendToPoem = appendToPoem;
        options.areWordsSelectedBegin = areWordsSelectedBegin;
        options.thisWordSelectedBegin = thisWordSelectedBegin;
        options.changeSelectionEffect = changeSelectionEffect;

        // parameter: status verbosity
        var svl = document.getElementById('statusVerbosityLevel');
        var statusVerbosity = svl.options[svl.selectedIndex].value;
        if ( statusVerbosity == 'silent' ) {
            statusVerbosity = 0;
        } else if ( statusVerbosity == 'terse' ) {
            statusVerbosity = 1;
        } else if ( statusVerbosity == 'verbose' ) {
            statusVerbosity = 2;
        }

        options.statusVerbosity = statusVerbosity;

        return options;

    };


    // this continues to read from the document dynamically, depending on the number of corpus(n) that exist
    this.getCorpora = function(capitalizeMethod, handlePunctuation) {

        // make array of corpora texts, and of corpora (percentage) weights
        var aCorporaTexts = new Array();
        var aCorporaWeights = new Array();
        var oTextArea = parent.control.document.getElementById('corpus1');
        var j = 1; // corpus counter
        while ( oTextArea != null ) {

            //var tempCorpusText = oTextArea.value;
            var tempCorpusText = self.editStringCleanCorpus( oTextArea.value, handlePunctuation );
            // remove caps from the corpus, if need be
            if ( capitalizeMethod == 'capitalizeNone' || capitalizeMethod == 'capitalizeCustom' ) {
                tempCorpusText = tempCorpusText.toLowerCase();
            }
            aCorporaTexts.push( tempCorpusText );

            // get corpora weight, add to array
            var currentP = 'percentageWeightCorpus' + j;
            aCorporaWeights.push(parent.control.document.getElementById(currentP).innerHTML);

            //debugOutput('corpus ' + j +' is: ' + tempCorpusText, statusVerbosity, 2);
            debug('corpus weight for ' + j +' is: ' + parent.control.document.getElementById(currentP).innerHTML, 2);

            j++;
            var currentC = 'corpus' + j;
            oTextArea = parent.control.document.getElementById(currentC);
        }

        var corpora = {
            texts: aCorporaTexts,
            weights: aCorporaWeights
        };

        return corpora;

    };

    // provide a buffer of one space around certain punctuation
    // replace newlines with spaces
    // replace multiple spaces with single space
    this.editStringCleanCorpus = function( inputText, handlePunctuation ) {

        // first, remove title comment (from initial '//' to first newline)
        if ( inputText.match(/^\/\//) ) {
            inputText = inputText.replace(/^\/\/.*\n/, '');
        }

        // place spaces at beginning and end of corpus
        inputText = ' ' + inputText + ' ';

        if ( handlePunctuation == 'noParen' || handlePunctuation == 'none' ) {
            // remove parens
            inputText = inputText.replace(/"/g, ' ');   // strip: '
            inputText = inputText.replace(/\(/g, ' ');
            inputText = inputText.replace(/\)/g, ' ');
            inputText = inputText.replace(/\[/g, ' ');
            inputText = inputText.replace(/\]/g, ' ');
            inputText = inputText.replace(/\{/g, ' ');
            inputText = inputText.replace(/\}/g, ' ');

            // remove asterisks, slashes, and carets
            inputText = inputText.replace(/\*/g, ' ');
            inputText = inputText.replace(/\//g, ' ');
            inputText = inputText.replace(/\^/g, ' ');
        }

        if ( handlePunctuation == 'none' ) {
            // remove certain punctuation (but not dashes and apostrophes)
            inputText = inputText.replace(/,/g, ' ');
            inputText = inputText.replace(/\./g, ' ');
            inputText = inputText.replace(/\?/g, ' ');
            inputText = inputText.replace(/!/g, ' ');
            inputText = inputText.replace(/;/g, ' ');
            inputText = inputText.replace(/:/g, ' ');
            inputText = inputText.replace(/--/g, ' ');
        }

        if ( handlePunctuation == 'all' || handlePunctuation == 'noParen' ) {
            // place spaces around parens
            // none of these will be in the text if 'noParen'
            inputText = inputText.replace(/"/g, ' " ');
            inputText = inputText.replace(/\(/g, ' \( ');
            inputText = inputText.replace(/\)/g, ' \) ');
            inputText = inputText.replace(/\[/g, ' \[ ');
            inputText = inputText.replace(/\]/g, ' \] ');
            inputText = inputText.replace(/\{/g, ' \{ ');
            inputText = inputText.replace(/\}/g, ' \} ');

            // collapse multiple repeated punctuations
            inputText = inputText.replace(/\.+/g, ' . ');
            inputText = inputText.replace(/\!+/g, ' ! ');
            inputText = inputText.replace(/\?+/g, ' ? ');

            // place spaces around certain punctuation (but not dashes and apostrophes)
            inputText = inputText.replace(/,/g, ' , ');
            inputText = inputText.replace(/\./g, ' \. ');
            inputText = inputText.replace(/\?/g, ' \? ');
            inputText = inputText.replace(/!/g, ' ! ');
            inputText = inputText.replace(/;/g, ' ; ');
            inputText = inputText.replace(/:/g, ' : ');
            inputText = inputText.replace(/--/g, ' -- ');
        }

        //inputText = inputText.replace(/\n/g, ' ');
        //inputText = inputText.replace(/\r/g, ' ');

        // collapse multiple newlines
        inputText = inputText.replace(/\r/g, '\n');
        inputText = inputText.replace(/\n+/g, ' \n ');

        // collapse multiple spaces
        inputText = inputText.replace(/ +/g, ' ');

        return inputText;
    };

    this.showHide = function() {
        // choose text for the show/hide link - can contain HTML (e.g. an image)
        var showText='Show';
        var hideText='Hide';

        // append show/hide links to the element directly preceding the element with a class of 'toggle'
        $('.toggle').prev().append(' (<a href="#" class="toggleLink">' + showText + '</a>)');

        // hide all of the elements with a class of 'toggle'
        $('.toggle').hide();

        // capture clicks on the toggle links
        $('a.toggleLink').click(
            function() {

                // change the link depending on whether the element is shown or hidden
                $(this).html ($(this).html() == hideText ? showText : hideText);

                // toggle the display - uncomment the next line for a basic 'accordion' style
                //$('.toggle').hide();$('a.toggleLink').html(showText);
                $(this).parent().next('.toggle').toggle('slow');

                // return false so any link destination is not followed
                return false;

            });
    },


    this.setCorpusTexts = function(){

        // TODO: namespace the methods
        $('#corpus1').val(shakespeare);
        $('#corpus2').val(tristantzara);
        $('#corpus3').val(lessig);
        $('#templateText').val(self.initialTemplate);

    };

    this.buildDropDown = function(id, selLength, selectedRow) {

        var s = document.createElement('select');
        s.id = id;

        for (var i = 0; i < selLength; i++) {
            var o = document.createElement('option');
            o.value = i;
            o.text = i;
            if (i == selectedRow) {
                o.setAttribute('selected', 'selected');
            }
            s.appendChild(o);
        }

        return s;

    };

    // NOTE: without line-break, things get screwy....
    this.initialTemplate = '[s] [s] [s] [s] [s] [s] [s] [s] [s] [s] [n]\n' +
        '[s] [s] [s] [s] [s] [s] [s] [s] [s] [s] [n]\n' +
        '[s] [s] [s] [s] [s] [s] [s] [s] [s] [s] [n]\n' +
        '[s] [s] [s] [s] [s] [s] [s] [s] [s] [s]';


    this.getExistingText = function() {

        var Element = function() {
            this.id = '';  // not currently populating; don't even know if it will be needed...
            this.text = '';
            this.backgroundColor = ''; // TODO: refactor this semantically once working
        };
        var elements = [];

        // 'w0' == first word element
        var oElement = parent.editor.document.getElementById('w0');

        var i = 0;

        while (oElement != null) {

            var el = new Element();
            el.text = oElement.value.toLowerCase();
            el.backgroundColor = oElement.style.backgroundColor;

            elements.push(el);

            i++;
            var currentId = 'w' + i;

            oElement = parent.editor.document.getElementById(currentId);
        }

        return elements;

    };

};


// remove quotations in a string
var editStringRemoveQuotations = function( inputText ) {

    inputText = inputText.replace(/'/g, '&#39;');
    inputText = inputText.replace(/'/g, '&#34;');  // handle '

    return inputText;
};

function toggleTemplate() {

    toggleVisibility('templateSpan');

}


function toggleStatus() {

    toggleVisibility('statusSpan');

}

function toggleCorpus() {

    toggleVisibility('corpusSpan');

}


function toggleOptions() {

    toggleVisibility('optionsSpan');

}

function toggleVisibility(targetId) {

    var elemStyle = parent.control.document.getElementById(targetId).style;

    elemStyle.visibility = (elemStyle.visibility == 'hidden' || elemStyle.visibility == '') ? 'visible' : 'hidden';
    elemStyle.display = (elemStyle.display == 'none' || elemStyle.display == '') ? 'block' : 'none';

}

function setVisibility(targetId, visible) {

    var elemStyle = parent.control.document.getElementById(targetId).style;

    elemStyle.visibility = (visible ? 'visible' : 'hidden');
    elemStyle.display = (visible ? 'block' : 'none');

}


// pass in document reference, since we're dealing with a frameset
// this abstracts the link to any particular frame
// although in practice, only one frame has these elements...
function addCorpus(doc) {

    var corpusNbr = 0;

    // find the new corpus number

    var oElement = doc.getElementById('corpus1');

    while ( oElement != null ) {
        corpusNbr++;
        var elementName = 'corpus' + corpusNbr;
        oElement = doc.getElementById(elementName);
    }
    var nextNumber = corpusNbr + 1;

    // at this point, corpusNbr = the number for the new corpus element

    // TODO: instead of outputting a string, let's build a real element, and bind it accordingly

    //var s = buildDropDown('weightCorpus' + corpusNbr, 10, 0);
    //$(s).bind('change', calculateWeights);

    //$('<p>Test</p>').insertBefore('.inner');
    //$(s).insertBefore('#addCorpus');

    // TODO: I think we can get rid of the breaks and non-breaking spaces
    // with some judicious use of CSS

    var toPrint = '<div id="corpusRegion' + corpusNbr + '">Corpus ' + corpusNbr +' : &nbsp; &nbsp; '
            + '<a href="javascript:clearCorpus("corpus' + corpusNbr + '")">clear</a>'
            + '&nbsp; &nbsp; weight: <select id="weightCorpus' + corpusNbr + '" onchange="javascript:calculateWeights()">'
            + '<option selected value="0">0<option value="1">1<option value="2">2<option value="3">3<option value="4">4'
            + '<option value="5">5<option value="6">6<option value="7">7<option value="8">8<option value="9">9'
            + '<option value="10">10</select>'
            + '&nbsp; (<span id="percentageWeightCorpus' + corpusNbr + '">0</span>%)'
            + '<br>\n'
            + '<textarea rows="4" cols="60" id="corpus' + corpusNbr + '">\n</textarea>\n'
            + '</div>\n';

    // workaround for dealing with magic-string text...
    var d = doc.createElement('div');
    d.innerHTML = toPrint;

    var ac = doc.getElementById('addCorpus');
    ac.parentNode.insertBefore(d, ac);


    //var elementIdName = 'additionalCorpus' + corpusNbr;
    //doc.getElementById( elementIdName ).innerHTML = toPrint;

}

function calculateWeights() {

    var oElement = parent.control.document.getElementById('weightCorpus1');
    var currentWeight, currentId;
    var i=1;
    var total=0;

    while ( oElement != null ) {
        currentWeight = parseInt(oElement.options[oElement.selectedIndex].value, 10);
        total += parseInt(currentWeight, 10);
        i++;
        currentId = 'weightCorpus' + i;
        oElement = parent.control.document.getElementById( currentId );
    }

    oElement = parent.control.document.getElementById('weightCorpus1');
    var oDisplay = parent.control.document.getElementById('percentageWeightCorpus1');
    i=1;
    while ( oElement != null ) {
        currentWeight = parseInt(oElement.options[oElement.selectedIndex].value, 10);
        var newWeight = Math.round((currentWeight/total) * 100 );
        oDisplay.innerHTML = newWeight;

        i++;
        currentId = 'weightCorpus' + i;
        var currentDisplay = 'percentageWeightCorpus' + i;
        oElement = parent.control.document.getElementById( currentId );
        oDisplay = parent.control.document.getElementById( currentDisplay );
    }

}

// selected means 'selected for re-generation
// not 'selected to keep'
// this takes some getting used to...
function setSelectedAll() {

    var oElement = parent.editor.document.getElementById('w0');
    var i=0;

    while ( oElement != null ) {
        oElement.style.backgroundColor = '#FF9999';

        i++;
        var currentId = 'w' + i;
        oElement = parent.editor.document.getElementById( currentId );
    }
}

function setSelectedNone() {

    var oElement = parent.editor.document.getElementById('w0');
    var i=0;

    while ( oElement != null ) {
        oElement.style.backgroundColor = 'transparent';

        i++;
        var currentId = 'w' + i;
        oElement = parent.editor.document.getElementById( currentId );
    }
}

function appendButton( editorString, wordToPrint, generatedWordsIndex, areWordsSelectedBegin, thisWordSelectedBegin, changeSelectionEffect ) {
    var buttonColor = '';
    var changeSelection = '';
    var changeSelection2 = '';

    // TODO: abstract these out to css-classes
    if ( thisWordSelectedBegin == 'startSelected' ) {
        buttonColor = '#FF9999';
    } else {
        buttonColor = 'transparent';
    }

    if ( changeSelectionEffect == 'togglesBetween' ) {
        changeSelection = 'onmouseover="javascript:switchColor(this)"';
    } else if ( changeSelectionEffect == 'changesOneWay' || changeSelectionEffect == 'changesOneWayPlusClick' ) {
        if ( areWordsSelectedBegin == 'startSelected' ) {
            changeSelection = 'onmouseover="javascript:switchToUnselected(this)"';
        } else {
            changeSelection = 'onmouseover="javascript:switchToSelected(this)"';
        }
    }

    if ( changeSelectionEffect == 'requiresClick' ) {
        changeSelection2 = ' onclick="javascript:switchColor(this)"';
    } else if ( changeSelectionEffect == 'changesOneWayPlusClick' ) {
        if ( areWordsSelectedBegin == 'startSelected' ) {
            changeSelection2 = ' onclick="javascript:switchToSelected(this)"';
        } else {
            changeSelection2 = ' onclick="javascript:switchToUnselected(this)"';
        }
    }

    return editorString + '<input type="button" id="w' + generatedWordsIndex + '" style="background-color:' + buttonColor + '" value="' + editStringRemoveQuotations(wordToPrint) + '" ' + changeSelection + changeSelection2 + '>';
}

// TODO: take text as a param, not hard-coded to form...
function exportText() {
    var TextExport = window.open('','TextExport');
    TextExport.document.writeln( '<html><body>' );
    TextExport.document.writeln( parent.display.document.getElementById('displayText').innerHTML );
    TextExport.document.writeln( '</body></html>' );
    TextExport.document.close();
}

// TODO: take text as a param, not hard-coded to form...
function exportDebug() {
    var DebugExport = window.open('','DebugExport');
    DebugExport.document.writeln( '<html><body><pre>' );
    DebugExport.document.writeln( parent.control.document.controlForm.debugOutput.value );
    DebugExport.document.writeln( '</pre></body></html>' );
    DebugExport.document.close();
}

function clearDebug() {
    parent.control.document.controlForm.debugOutput.value = '';
}


// TO DO: need to debug less/better, or IE is very slow...
// print to debut output window
// TODO: find a way to set the verbosity at the beginning of generate, and not pass it all over creation and back
function debugOutput( output, statusVerbosity, thisVerbosity ) {

    //     parent.control.document.getElementById('debugOutput').value =   parent.control.document.getElementById('debugOutput').value + '***' + statusVerbosity + ' ' + thisVerbosity + '\n';


    // if ( statusVerbosity >= thisVerbosity ) {
    //    parent.control.document.getElementById('debugOutput').value + 'TRUE \n';
    // } else {
    //    parent.control.document.getElementById('debugOutput').value + ' FALSE\n';
    // }

    var area = parent.control.document.getElementById('debugOutput');

    if ( area && statusVerbosity >= thisVerbosity ) {
        area.value =  area.value + output + '\n';
    }

}



function clearCorpus( corpusName ) {
    parent.control.document.getElementById(corpusName).value = '';
}


function clearTemplateText() {
    parent.control.document.controlForm.templateText.value = '';
}


// TODO: all the clear textareas above should be like this (I was learning javascript as I did this...)
function clearTextarea( elementName ) {
    parent.control.document.getElementById(elementName).value = '';
}


// set base form window
function setBaseForm(dropdownForm) {

    var index = dropdownForm.selectedIndex;
    var selectedValue = dropdownForm.options[index].value;

    var text = parent.control.document.controlForm.templateText;

    if ( selectedValue == 'couplet' ) {
        text.value = '[s] [s] [s] [s] [s] [s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] [s] [s] [s] [s] [s] ';
    } else if ( selectedValue == 'quatrain' ) {
        text.value = '[s] [s] [s] [s] [s] [s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] [s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] [s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] [s] [s] [s] [s] [s]';
    } else if ( selectedValue == 'blankSonnet' ) {
        text.value = '[s] [s] [s] [s] [s] [s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] [s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] [s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] [s] [s] [s] [s] [s] [n]\n[n]\n[s] [s] [s] [s] [s] [s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] [s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] [s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] [s] [s] [s] [s] [s] [n]\n[n]\n[s] [s] [s] [s] [s] [s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] [s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] [s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] [s] [s] [s] [s] [s] [n]\n[n]\n[s] [s] [s] [s] [s] [s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] [s] [s] [s] [s] [s]';
    } else if ( selectedValue == 'haiku' ) {
        text.value = '[s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] ';
    } else if ( selectedValue == 'tanka' ) {
        text.value = '[s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] [n]\n[n]\n[s] [s] [s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] [s] [s] ';
    } else if ( selectedValue == 'renga' ) {
        text.value = '[s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] [n]\n[n]\n[s] [s] [s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] [s] [s] [n]\n[n]\n[s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] [s] [s] [n]\n[s] [s] [s] [s] [s] ';
    }
}
