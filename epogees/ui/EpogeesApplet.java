
package epogees.ui;

import epogees.generation.*;
import epogees.model.*;
import epogees.model.language.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.JSlider;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.JComboBox;


public class EpogeesApplet extends javax.swing.JApplet {

    final static String SHAKESPEARE_CLASS_BIGRAM = "Shakespeare's Sonnets (Class-Based Bigram)";
    final static String SHAKESPEARE_WORD_BIGRAM = "Shakespeare's Sonnets (Word-Based Bigram)";

    String fileLocation = "shakespeare-sonnetsAll-tagged.txt";
    String typeBigramFileLocation = "shakespeare-sonnetsAll.txt";
    String dictionary1Filename = "cmudict.0.7a";
    String dictionary2Filename = "additional-pronunciations.txt";

    boolean isLoaded = false;
    PhonemeEvaluation pe;
    LanguageModel lm;
    Generation gen;
    PhonemeModel pm;
    // TO DO: this should not be public
    public RhymeModel rm;

    String whichModelLoaded = new String();
    int userModelNumber = 1;
    int defaultNumberOfModels = 2;

    // should probably be moved to Generation
//    int linesToGenerate = 1;
//    String searchAlgorithm = STOCHASTIC_BEAM_SEARCH;

    // INITIALIZATION METHODS

    /** Initializes the applet FlowApplet */
    public void init() {
        try {
            java.awt.EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    initComponents();
                    setup();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // load the models
    public void setup() {

        System.out.println("Loading initial language model");
//        lm = new ClassBasedBigramModel();
        lm = new WordBasedBigramModel();
//        readJarTaggedFile( "data/" + fileLocation);
        readJarModelFile( "../data/" + typeBigramFileLocation);
//        System.out.println(" read " + fileLocation + ", built class-based bigram language model");
        System.out.println(" read " + typeBigramFileLocation + ", built word-based bigram language model");
        //whichModelLoaded = SHAKESPEARE_CLASS_BIGRAM;
        whichModelLoaded = SHAKESPEARE_WORD_BIGRAM;

        readPhonemeAndRhymeModels();
        
        isLoaded = true;
    }

    // read phoneme and rhyme models.  will be called when making models from user
    public void readPhonemeAndRhymeModels() {
        System.out.println("Loading phoneme model");
        pe = new PhonemeEvaluation();
        //readJarPhonemeFile("data/cmudict.0.7a");
        //readJarPhonemeFile("data/additional-pronunciations.txt");
        readJarPhonemeFile("../data/" + dictionary1Filename);
        readJarPhonemeFile("../data/" + dictionary2Filename);
        System.out.println(" read " + dictionary1Filename );
        System.out.println(" read " + dictionary2Filename );

        System.out.println("Loading rhyme model");
        rm = new RhymeModel( pe );
        readJarRhymeFile("../data/" + dictionary1Filename);
        readJarRhymeFile("../data/" + dictionary2Filename);
        System.out.println(" read " + dictionary1Filename );
        System.out.println(" read " + dictionary2Filename );

        gen = new Generation( pe, lm, rm  );

        setInitialPhonemeValues();
        
        pm = pe.getPhonemeModel();
    }

    // TO DO: would be better to query these from the controls.  then wouldn't need "isLoaded" check above.
    public void setInitialPhonemeValues() {
        // internal rhyme
        pe.setAlliterationWeight(10);
        pe.setAssonanceWeight(9);
        // back vowels
        pe.setPhonemesAndWeightsPairs( UH, 10 );
        pe.setPhonemesAndWeightsPairs( AO, 10 );
        pe.setPhonemesAndWeightsPairs( ER, 10 );
        pe.setPhonemesAndWeightsPairs( UW, 10 );
        pe.setPhonemesAndWeightsPairs( AW, 10 );
        pe.setPhonemesAndWeightsPairs( AY, 10 );
        // central vowels
        pe.setPhonemesAndWeightsPairs( AH, 5 );
        pe.setPhonemesAndWeightsPairs( EH, 5 );
        pe.setPhonemesAndWeightsPairs( OW, 5 );
        pe.setPhonemesAndWeightsPairs( OY, 5 );
        // front vowels
        pe.setPhonemesAndWeightsPairs( AA, 1 );
        pe.setPhonemesAndWeightsPairs( AE, 1 );
        pe.setPhonemesAndWeightsPairs( EY, 1 );
        pe.setPhonemesAndWeightsPairs( IH, 1 );
        pe.setPhonemesAndWeightsPairs( IY, 1 );

        // affricatives
        pe.setPhonemesAndWeightsPairs( CH, 0 );
        pe.setPhonemesAndWeightsPairs( JH, 0 );
        // nasals
        pe.setPhonemesAndWeightsPairs( M, 0 );
        pe.setPhonemesAndWeightsPairs( N, 0 );
        pe.setPhonemesAndWeightsPairs( NG, 0 );
        // approximants
        pe.setPhonemesAndWeightsPairs( L, 5 );
        pe.setPhonemesAndWeightsPairs( R, 5 );
        pe.setPhonemesAndWeightsPairs( W, 5 );
        pe.setPhonemesAndWeightsPairs( Y, 5 );
        // plosives - voiced
        pe.setPhonemesAndWeightsPairs( B, 3 );
        pe.setPhonemesAndWeightsPairs( D, 3 );
        pe.setPhonemesAndWeightsPairs( G, 3 );
        // plosives - voiceless
        pe.setPhonemesAndWeightsPairs( P, 7 );
        pe.setPhonemesAndWeightsPairs( T, 7 );
        pe.setPhonemesAndWeightsPairs( K, 7 );
        // fricatives - sibilant
        pe.setPhonemesAndWeightsPairs( S, 1 );
        pe.setPhonemesAndWeightsPairs( SH, 1 );
        pe.setPhonemesAndWeightsPairs( Z, 1 );
        pe.setPhonemesAndWeightsPairs( ZH, 1 );
        // fricatives - non-sibilant
        pe.setPhonemesAndWeightsPairs( F, 3 );
        pe.setPhonemesAndWeightsPairs( TH, 3 );
        pe.setPhonemesAndWeightsPairs( DH, 3 );
        pe.setPhonemesAndWeightsPairs( V, 3 );
        pe.setPhonemesAndWeightsPairs( HH, 3 );
    }


    // FILE READING METHODS

    // read a tagged file, count the word classes, create model of words per class type
    public void readTaggedFile( String fileLocation ) {
        BufferedReader inputStream = null;
        String currentLine;
        List allLines = new ArrayList();

        try {
            URL source = new URL(getCodeBase(), fileLocation);
            inputStream = new BufferedReader( new InputStreamReader(source.openStream()) );

            while ( (currentLine = inputStream.readLine()) != null ) {
                allLines.add(currentLine);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        lm.readModelList(allLines);
    }

    // read a tagged file, count the word classes, create model of words per class type
    public void readJarModelFile( String fileLocation ) {
        BufferedReader inputStream = null;
        String currentLine;
        List allLines = new ArrayList();

        try {
            //URL source = new URL(getCodeBase(), fileLocation);
            InputStream is = getClass().getResourceAsStream(fileLocation);
            inputStream = new BufferedReader( new InputStreamReader(is) );

            while ( (currentLine = inputStream.readLine()) != null ) {
                allLines.add(currentLine);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        lm.readModelList(allLines);
    }

    // read a tagged file, count the word classes, create model of words per class type
    public void readPhonemeFile( String fileLocation ) {
        BufferedReader inputStream = null;
        String currentLine;
        List allLines = new ArrayList();

        try {
            URL source = new URL(getCodeBase(), fileLocation);
            inputStream = new BufferedReader( new InputStreamReader(source.openStream()) );

            while ( (currentLine = inputStream.readLine()) != null ) {
                allLines.add(currentLine);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        pe.readPhonemeModel(allLines, lm);
    }

    // read phoneme file from jar file, make phoneme model
    public void readJarPhonemeFile( String fileLocation ) {
        BufferedReader inputStream = null;
        String currentLine;
        List allLines = new ArrayList();

        try {
            //URL source = new URL(getCodeBase(), fileLocation);
            InputStream is = getClass().getResourceAsStream(fileLocation);
            inputStream = new BufferedReader( new InputStreamReader(is) );

            while ( (currentLine = inputStream.readLine()) != null ) {
                allLines.add(currentLine);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        pe.readPhonemeModel(allLines, lm);
    }

    // read phoneme file from jar file, make phoneme model
    public void readJarRhymeFile( String fileLocation ) {
        BufferedReader inputStream = null;
        String currentLine;
        List allLines = new ArrayList();

        try {
            //URL source = new URL(getCodeBase(), fileLocation);
            InputStream is = getClass().getResourceAsStream(fileLocation);
            inputStream = new BufferedReader( new InputStreamReader(is) );

            while ( (currentLine = inputStream.readLine()) != null ) {
                allLines.add(currentLine);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        rm.readRhymeList(allLines, lm);
    }

    // read a zipped file, return a string
    public void readZippedPhonemeFile(String inputFileLocation) {
        //StringBuffer toReturn = new StringBuffer();
        String currentLine = new String();
        List allLines = new ArrayList();

        try {
            // look through zipfile to identify individual files in it
            ZipFile zf = new ZipFile(inputFileLocation);
            Enumeration e = zf.entries();
            while (e.hasMoreElements()) {
                ZipEntry ze = (ZipEntry)e.nextElement();

                // read a line at a time from the file
                BufferedInputStream bis = new BufferedInputStream(zf.getInputStream(ze));
                BufferedReader br = new BufferedReader(new InputStreamReader(bis));
                while ((currentLine = br.readLine()) != null) {
                    //toReturn.append(currentLine + "\n");
                    allLines.add(currentLine);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //return toReturn.toString();
        pe.readPhonemeModel(allLines, lm);
    }




    // GUI ACTION

    /** This method is called from within the init() method to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        searchButtonGroup1 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel21 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextArea4 = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jPanel22 = new javax.swing.JPanel();
        jButton8 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jPanel23 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        jPanel18 = new javax.swing.JPanel();
        jPanel19 = new javax.swing.JPanel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox();
        jComboBox3 = new javax.swing.JComboBox();
        jComboBox4 = new javax.swing.JComboBox();
        jCheckBox1 = new javax.swing.JCheckBox();
        jPanel20 = new javax.swing.JPanel();
        jLabel46 = new javax.swing.JLabel();
        jComboBox5 = new javax.swing.JComboBox();
        jLabel51 = new javax.swing.JLabel();
        jComboBox7 = new javax.swing.JComboBox();
        jLabel52 = new javax.swing.JLabel();
        jCheckBox5 = new javax.swing.JCheckBox();
        jLabel53 = new javax.swing.JLabel();
        jCheckBox6 = new javax.swing.JCheckBox();
        jPanel25 = new javax.swing.JPanel();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        jComboBox6 = new javax.swing.JComboBox();
        jLabel49 = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        jCheckBox2 = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jSlider29 = new javax.swing.JSlider();
        jLabel29 = new javax.swing.JLabel();
        jSlider32 = new javax.swing.JSlider();
        jLabel32 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jSlider34 = new javax.swing.JSlider();
        jLabel35 = new javax.swing.JLabel();
        jSlider35 = new javax.swing.JSlider();
        jPanel2 = new javax.swing.JPanel();
        jSlider28 = new javax.swing.JSlider();
        jSlider27 = new javax.swing.JSlider();
        jSlider30 = new javax.swing.JSlider();
        jLabel30 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jSlider19 = new javax.swing.JSlider();
        jLabel19 = new javax.swing.JLabel();
        jSlider25 = new javax.swing.JSlider();
        jLabel25 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jSlider22 = new javax.swing.JSlider();
        jLabel22 = new javax.swing.JLabel();
        jSlider37 = new javax.swing.JSlider();
        jLabel37 = new javax.swing.JLabel();
        jSlider38 = new javax.swing.JSlider();
        jLabel38 = new javax.swing.JLabel();
        jSlider24 = new javax.swing.JSlider();
        jLabel24 = new javax.swing.JLabel();
        jSlider23 = new javax.swing.JSlider();
        jLabel23 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jSlider6 = new javax.swing.JSlider();
        jLabel6 = new javax.swing.JLabel();
        jSlider4 = new javax.swing.JSlider();
        jSlider8 = new javax.swing.JSlider();
        jSlider14 = new javax.swing.JSlider();
        jLabel4 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jSlider15 = new javax.swing.JSlider();
        jLabel15 = new javax.swing.JLabel();
        jSlider5 = new javax.swing.JSlider();
        jLabel5 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jSlider40 = new javax.swing.JSlider();
        jLabel40 = new javax.swing.JLabel();
        jSlider39 = new javax.swing.JSlider();
        jLabel39 = new javax.swing.JLabel();
        jSlider33 = new javax.swing.JSlider();
        jLabel33 = new javax.swing.JLabel();
        jSlider41 = new javax.swing.JSlider();
        jLabel41 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jSlider31 = new javax.swing.JSlider();
        jLabel31 = new javax.swing.JLabel();
        jSlider36 = new javax.swing.JSlider();
        jLabel36 = new javax.swing.JLabel();
        jSlider26 = new javax.swing.JSlider();
        jLabel26 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jSlider18 = new javax.swing.JSlider();
        jLabel18 = new javax.swing.JLabel();
        jSlider20 = new javax.swing.JSlider();
        jLabel20 = new javax.swing.JLabel();
        jSlider21 = new javax.swing.JSlider();
        jLabel21 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jSlider17 = new javax.swing.JSlider();
        jSlider16 = new javax.swing.JSlider();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jCheckBox3 = new javax.swing.JCheckBox();
        jPanel15 = new javax.swing.JPanel();
        jSlider11 = new javax.swing.JSlider();
        jLabel11 = new javax.swing.JLabel();
        jSlider12 = new javax.swing.JSlider();
        jLabel12 = new javax.swing.JLabel();
        jSlider9 = new javax.swing.JSlider();
        jLabel9 = new javax.swing.JLabel();
        jSlider2 = new javax.swing.JSlider();
        jLabel2 = new javax.swing.JLabel();
        jSlider1 = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        jPanel16 = new javax.swing.JPanel();
        jSlider10 = new javax.swing.JSlider();
        jLabel10 = new javax.swing.JLabel();
        jSlider3 = new javax.swing.JSlider();
        jLabel3 = new javax.swing.JLabel();
        jSlider13 = new javax.swing.JSlider();
        jLabel13 = new javax.swing.JLabel();
        jSlider7 = new javax.swing.JSlider();
        jLabel7 = new javax.swing.JLabel();
        jPanel17 = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jPanel12 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jComboBox1 = new javax.swing.JComboBox();
        jPanel14 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea3 = new javax.swing.JTextArea();
        jTextField1 = new javax.swing.JTextField();
        jLabel42 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jCheckBox4 = new javax.swing.JCheckBox();
        jPanel24 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();

        getContentPane().setLayout(null);

        jPanel21.setLayout(null);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jPanel21.add(jScrollPane1);
        jScrollPane1.setBounds(10, 20, 450, 510);

        jTextArea4.setBackground(new java.awt.Color(204, 204, 204));
        jTextArea4.setColumns(20);
        jTextArea4.setEditable(false);
        jTextArea4.setRows(5);
        jScrollPane4.setViewportView(jTextArea4);

        jPanel21.add(jScrollPane4);
        jScrollPane4.setBounds(480, 20, 560, 360);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Generate"));
        jPanel3.setLayout(null);

        jButton1.setBackground(new java.awt.Color(255, 102, 102));
        jButton1.setText("Lines"); // NOI18N
        jButton1.setActionCommand("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton1);
        jButton1.setBounds(20, 30, 80, 23);

        jButton14.setText("Word");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertWord(evt);
            }
        });
        jPanel3.add(jButton14);
        jButton14.setBounds(20, 80, 80, 23);

        jPanel21.add(jPanel3);
        jPanel3.setBounds(480, 390, 120, 140);

        jPanel22.setBorder(javax.swing.BorderFactory.createTitledBorder("Generate Rhyming"));
        jPanel22.setLayout(null);

        jButton8.setText("Line");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        jPanel22.add(jButton8);
        jButton8.setBounds(20, 30, 80, 23);

        jButton7.setText("Word"); // NOI18N
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        jPanel22.add(jButton7);
        jButton7.setBounds(20, 80, 80, 23);

        jPanel21.add(jPanel22);
        jPanel22.setBounds(610, 390, 120, 140);

        jPanel23.setBorder(javax.swing.BorderFactory.createTitledBorder("Analyze"));
        jPanel23.setLayout(null);

        jButton6.setText("Phonemes"); // NOI18N
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phoneCheck(evt);
            }
        });
        jPanel23.add(jButton6);
        jButton6.setBounds(160, 30, 110, 23);

        jButton15.setText("Next Words");
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findNextWords(evt);
            }
        });
        jPanel23.add(jButton15);
        jButton15.setBounds(20, 30, 110, 23);

        jButton16.setText("Prev Words");
        jButton16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findtPrevWords(evt);
            }
        });
        jPanel23.add(jButton16);
        jButton16.setBounds(20, 80, 110, 23);

        jPanel21.add(jPanel23);
        jPanel23.setBounds(740, 390, 300, 140);

        jTabbedPane1.addTab("Output", jPanel21);

        jPanel18.setLayout(null);

        jPanel19.setBorder(javax.swing.BorderFactory.createTitledBorder("Line Generation Algorithm"));
        jPanel19.setLayout(null);

        searchButtonGroup1.add(jRadioButton1);
        jRadioButton1.setText(" Random Sampling"); // NOI18N
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setSearchAlgNRandom(evt);
            }
        });
        jPanel19.add(jRadioButton1);
        jRadioButton1.setBounds(30, 40, 150, 23);

        searchButtonGroup1.add(jRadioButton2);
        jRadioButton2.setSelected(true);
        jRadioButton2.setText(" Stochastic Beam Search"); // NOI18N
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setSearchAlgStochastic(evt);
            }
        });
        jPanel19.add(jRadioButton2);
        jRadioButton2.setBounds(30, 120, 170, 23);

        jLabel43.setText("Number of Iterations"); // NOI18N
        jPanel19.add(jLabel43);
        jLabel43.setBounds(60, 200, 130, 20);

        jLabel44.setText("Number of Samples"); // NOI18N
        jPanel19.add(jLabel44);
        jLabel44.setBounds(60, 70, 120, 20);

        jLabel45.setText("Population Size"); // NOI18N
        jPanel19.add(jLabel45);
        jLabel45.setBounds(60, 160, 100, 20);

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "5", "10", "20", "30" }));
        jComboBox2.setSelectedIndex(1);
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchNRandPopulation(evt);
            }
        });
        jPanel19.add(jComboBox2);
        jComboBox2.setBounds(200, 70, 50, 20);

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "5", "10", "20", "30", "50", "100" }));
        jComboBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchStochNumGenerations(evt);
            }
        });
        jPanel19.add(jComboBox3);
        jComboBox3.setBounds(200, 200, 50, 20);

        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "6", "10", "20", "30", "50" }));
        jComboBox4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchStochasticBeamPopulation(evt);
            }
        });
        jPanel19.add(jComboBox4);
        jComboBox4.setBounds(200, 160, 50, 20);

        jCheckBox1.setText(" Show Details in stdout");
        jCheckBox1.setToolTipText("keep this off unless debugging"); // NOI18N
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeStochasticShowDetails(evt);
            }
        });
        jPanel19.add(jCheckBox1);
        jCheckBox1.setBounds(30, 260, 190, 23);

        jPanel18.add(jPanel19);
        jPanel19.setBounds(340, 10, 280, 380);

        jPanel20.setBorder(javax.swing.BorderFactory.createTitledBorder("Stanza Options"));
        jPanel20.setLayout(null);

        jLabel46.setText("Rhyme Scheme"); // NOI18N
        jPanel20.add(jLabel46);
        jLabel46.setBounds(40, 80, 110, 20);

        jComboBox5.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "4", "8", "14" }));
        jComboBox5.setSelectedIndex(2);
        jComboBox5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generationNumberLines(evt);
            }
        });
        jPanel20.add(jComboBox5);
        jComboBox5.setBounds(200, 40, 60, 20);

        jLabel51.setText("Number of Lines"); // NOI18N
        jPanel20.add(jLabel51);
        jLabel51.setBounds(40, 40, 110, 20);

        jComboBox7.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "aabb", "abab", "None" }));
        jComboBox7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateRhymeScheme(evt);
            }
        });
        jPanel20.add(jComboBox7);
        jComboBox7.setBounds(200, 80, 60, 20);

        jLabel52.setText("Enjambment");
        jPanel20.add(jLabel52);
        jLabel52.setBounds(50, 130, 80, 14);

        jCheckBox5.setSelected(true);
        jCheckBox5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setEnjambment(evt);
            }
        });
        jPanel20.add(jCheckBox5);
        jCheckBox5.setBounds(200, 120, 50, 30);

        jLabel53.setText("Trailing Newline");
        jPanel20.add(jLabel53);
        jLabel53.setBounds(40, 170, 90, 14);

        jCheckBox6.setSelected(true);
        jCheckBox6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                switchTrailingNewline(evt);
            }
        });
        jPanel20.add(jCheckBox6);
        jCheckBox6.setBounds(200, 161, 30, 30);

        jPanel18.add(jPanel20);
        jPanel20.setBounds(10, 10, 320, 230);

        jPanel25.setBorder(javax.swing.BorderFactory.createTitledBorder("Line Options"));
        jPanel25.setLayout(null);

        jLabel47.setText("(approximate)");
        jPanel25.add(jLabel47);
        jLabel47.setBounds(30, 50, 80, 14);

        jLabel48.setText("Accented Vowels per Line"); // NOI18N
        jPanel25.add(jLabel48);
        jLabel48.setBounds(30, 30, 150, 20);

        jComboBox6.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "18", "36", "72" }));
        jComboBox6.setSelectedIndex(6);
        jComboBox6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeNumberAccentedVowels(evt);
            }
        });
        jPanel25.add(jComboBox6);
        jComboBox6.setBounds(210, 40, 60, 20);

        jLabel49.setText("Include Words Missing "); // NOI18N
        jPanel25.add(jLabel49);
        jLabel49.setBounds(30, 90, 150, 20);

        jLabel50.setText("Phoneme Models"); // NOI18N
        jPanel25.add(jLabel50);
        jLabel50.setBounds(30, 110, 110, 20);

        jCheckBox2.setSelected(true);
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeEstimateMissing(evt);
            }
        });
        jPanel25.add(jCheckBox2);
        jCheckBox2.setBounds(210, 100, 30, 21);

        jPanel18.add(jPanel25);
        jPanel25.setBounds(10, 240, 320, 150);

        jTabbedPane1.addTab("Generation Options", jPanel18);

        jPanel1.setLayout(null);

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Approximants"));
        jPanel11.setLayout(null);

        jSlider29.setMajorTickSpacing(10);
        jSlider29.setMaximum(10);
        jSlider29.setMinorTickSpacing(1);
        jSlider29.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider29.setPaintTicks(true);
        jSlider29.setSnapToTicks(true);
        jSlider29.setToolTipText("L (ex: lee = L IY)"); // NOI18N
        jSlider29.setValue(2);
        jSlider29.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel11.add(jSlider29);
        jSlider29.setBounds(10, 20, 31, 200);

        jLabel29.setText("L"); // NOI18N
        jPanel11.add(jLabel29);
        jLabel29.setBounds(25, 230, 10, 14);

        jSlider32.setMajorTickSpacing(10);
        jSlider32.setMaximum(10);
        jSlider32.setMinorTickSpacing(1);
        jSlider32.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider32.setPaintTicks(true);
        jSlider32.setSnapToTicks(true);
        jSlider32.setToolTipText("R (ex: read = R IY D)"); // NOI18N
        jSlider32.setValue(2);
        jSlider32.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel11.add(jSlider32);
        jSlider32.setBounds(50, 20, 31, 200);

        jLabel32.setText("R"); // NOI18N
        jPanel11.add(jLabel32);
        jLabel32.setBounds(60, 230, 10, 14);

        jLabel34.setText("Y"); // NOI18N
        jPanel11.add(jLabel34);
        jLabel34.setBounds(140, 230, 10, 14);

        jSlider34.setMajorTickSpacing(10);
        jSlider34.setMaximum(10);
        jSlider34.setMinorTickSpacing(1);
        jSlider34.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider34.setPaintTicks(true);
        jSlider34.setSnapToTicks(true);
        jSlider34.setToolTipText("Y (ex: yield = Y IY L D)"); // NOI18N
        jSlider34.setValue(2);
        jSlider34.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel11.add(jSlider34);
        jSlider34.setBounds(130, 20, 31, 200);

        jLabel35.setText("W"); // NOI18N
        jPanel11.add(jLabel35);
        jLabel35.setBounds(100, 230, 20, 14);

        jSlider35.setMajorTickSpacing(10);
        jSlider35.setMaximum(10);
        jSlider35.setMinorTickSpacing(1);
        jSlider35.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider35.setPaintTicks(true);
        jSlider35.setSnapToTicks(true);
        jSlider35.setToolTipText("W (ex: we = W IY)"); // NOI18N
        jSlider35.setValue(2);
        jSlider35.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel11.add(jSlider35);
        jSlider35.setBounds(90, 20, 31, 200);

        jPanel1.add(jPanel11);
        jPanel11.setBounds(10, 270, 170, 260);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Nasals"));
        jPanel2.setLayout(null);

        jSlider28.setMajorTickSpacing(10);
        jSlider28.setMaximum(10);
        jSlider28.setMinorTickSpacing(1);
        jSlider28.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider28.setPaintTicks(true);
        jSlider28.setSnapToTicks(true);
        jSlider28.setToolTipText("M (ex: me = M IY)"); // NOI18N
        jSlider28.setValue(0);
        jSlider28.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel2.add(jSlider28);
        jSlider28.setBounds(10, 20, 31, 200);

        jSlider27.setMajorTickSpacing(10);
        jSlider27.setMaximum(10);
        jSlider27.setMinorTickSpacing(1);
        jSlider27.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider27.setPaintTicks(true);
        jSlider27.setSnapToTicks(true);
        jSlider27.setToolTipText("N (ex: knee = N IY)"); // NOI18N
        jSlider27.setValue(0);
        jSlider27.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel2.add(jSlider27);
        jSlider27.setBounds(50, 20, 31, 200);

        jSlider30.setMajorTickSpacing(10);
        jSlider30.setMaximum(10);
        jSlider30.setMinorTickSpacing(1);
        jSlider30.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider30.setPaintTicks(true);
        jSlider30.setSnapToTicks(true);
        jSlider30.setToolTipText("NG (ex: ping = P IH NG)"); // NOI18N
        jSlider30.setValue(0);
        jSlider30.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel2.add(jSlider30);
        jSlider30.setBounds(90, 20, 31, 200);

        jLabel30.setText("NG"); // NOI18N
        jPanel2.add(jLabel30);
        jLabel30.setBounds(90, 230, 20, 14);

        jLabel27.setText("N"); // NOI18N
        jPanel2.add(jLabel27);
        jLabel27.setBounds(60, 230, 10, 14);

        jLabel28.setText("M"); // NOI18N
        jPanel2.add(jLabel28);
        jLabel28.setBounds(20, 230, 10, 14);

        jPanel1.add(jPanel2);
        jPanel2.setBounds(910, 270, 130, 260);

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Affricatives"));
        jPanel10.setLayout(null);

        jSlider19.setMajorTickSpacing(10);
        jSlider19.setMaximum(10);
        jSlider19.setMinorTickSpacing(1);
        jSlider19.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider19.setPaintTicks(true);
        jSlider19.setSnapToTicks(true);
        jSlider19.setToolTipText("CH (ex: cheese = CH IY Z)"); // NOI18N
        jSlider19.setValue(0);
        jSlider19.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel10.add(jSlider19);
        jSlider19.setBounds(10, 20, 31, 200);

        jLabel19.setText("CH"); // NOI18N
        jPanel10.add(jLabel19);
        jLabel19.setBounds(20, 230, 20, 14);

        jSlider25.setMajorTickSpacing(10);
        jSlider25.setMaximum(10);
        jSlider25.setMinorTickSpacing(1);
        jSlider25.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider25.setPaintTicks(true);
        jSlider25.setSnapToTicks(true);
        jSlider25.setToolTipText("JH (ex: gee = JH IY)"); // NOI18N
        jSlider25.setValue(0);
        jSlider25.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel10.add(jSlider25);
        jSlider25.setBounds(50, 20, 31, 200);

        jLabel25.setText("JH"); // NOI18N
        jPanel10.add(jLabel25);
        jLabel25.setBounds(60, 230, 20, 14);

        jPanel1.add(jPanel10);
        jPanel10.setBounds(820, 270, 90, 260);

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Fricatives - Non-sibilant"));
        jPanel9.setLayout(null);

        jSlider22.setMajorTickSpacing(10);
        jSlider22.setMaximum(10);
        jSlider22.setMinorTickSpacing(1);
        jSlider22.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider22.setPaintTicks(true);
        jSlider22.setSnapToTicks(true);
        jSlider22.setToolTipText("F (ex: fee = F IY)"); // NOI18N
        jSlider22.setValue(3);
        jSlider22.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel9.add(jSlider22);
        jSlider22.setBounds(10, 20, 31, 200);

        jLabel22.setText("F"); // NOI18N
        jPanel9.add(jLabel22);
        jLabel22.setBounds(20, 230, 6, 14);

        jSlider37.setMajorTickSpacing(10);
        jSlider37.setMaximum(10);
        jSlider37.setMinorTickSpacing(1);
        jSlider37.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider37.setPaintTicks(true);
        jSlider37.setSnapToTicks(true);
        jSlider37.setToolTipText("TH (ex: theta = TH EY T AH)"); // NOI18N
        jSlider37.setValue(3);
        jSlider37.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel9.add(jSlider37);
        jSlider37.setBounds(50, 20, 31, 200);

        jLabel37.setText("TH"); // NOI18N
        jPanel9.add(jLabel37);
        jLabel37.setBounds(53, 230, 20, 14);

        jSlider38.setMajorTickSpacing(10);
        jSlider38.setMaximum(10);
        jSlider38.setMinorTickSpacing(1);
        jSlider38.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider38.setPaintTicks(true);
        jSlider38.setSnapToTicks(true);
        jSlider38.setToolTipText("V (ex: vee = V IY)"); // NOI18N
        jSlider38.setValue(3);
        jSlider38.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel9.add(jSlider38);
        jSlider38.setBounds(130, 20, 31, 200);

        jLabel38.setText("V"); // NOI18N
        jPanel9.add(jLabel38);
        jLabel38.setBounds(136, 230, 10, 14);

        jSlider24.setMajorTickSpacing(10);
        jSlider24.setMaximum(10);
        jSlider24.setMinorTickSpacing(1);
        jSlider24.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider24.setPaintTicks(true);
        jSlider24.setSnapToTicks(true);
        jSlider24.setToolTipText("HH (ex he = HH IY)"); // NOI18N
        jSlider24.setValue(3);
        jSlider24.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel9.add(jSlider24);
        jSlider24.setBounds(170, 20, 31, 200);

        jLabel24.setText("HH"); // NOI18N
        jPanel9.add(jLabel24);
        jLabel24.setBounds(174, 230, 20, 14);

        jSlider23.setMajorTickSpacing(10);
        jSlider23.setMaximum(10);
        jSlider23.setMinorTickSpacing(1);
        jSlider23.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider23.setPaintTicks(true);
        jSlider23.setSnapToTicks(true);
        jSlider23.setToolTipText("DH (ex: thee = DH IY)"); // NOI18N
        jSlider23.setValue(3);
        jSlider23.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel9.add(jSlider23);
        jSlider23.setBounds(90, 20, 31, 200);

        jLabel23.setText("DH"); // NOI18N
        jPanel9.add(jLabel23);
        jLabel23.setBounds(94, 230, 20, 14);

        jPanel1.add(jPanel9);
        jPanel9.setBounds(610, 270, 210, 260);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Back Vowels"));
        jPanel5.setLayout(null);

        jSlider6.setMajorTickSpacing(10);
        jSlider6.setMaximum(10);
        jSlider6.setMinorTickSpacing(1);
        jSlider6.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider6.setPaintTicks(true);
        jSlider6.setSnapToTicks(true);
        jSlider6.setToolTipText("AO (ex: ought = AO T)"); // NOI18N
        jSlider6.setValue(10);
        jSlider6.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel5.add(jSlider6);
        jSlider6.setBounds(60, 20, 31, 200);

        jLabel6.setText("AO"); // NOI18N
        jPanel5.add(jLabel6);
        jLabel6.setBounds(70, 230, 20, 14);

        jSlider4.setMajorTickSpacing(10);
        jSlider4.setMaximum(10);
        jSlider4.setMinorTickSpacing(1);
        jSlider4.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider4.setPaintTicks(true);
        jSlider4.setSnapToTicks(true);
        jSlider4.setToolTipText("AY (ex: hide = HH AY D)"); // NOI18N
        jSlider4.setValue(10);
        jSlider4.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel5.add(jSlider4);
        jSlider4.setBounds(220, 20, 31, 200);

        jSlider8.setMajorTickSpacing(10);
        jSlider8.setMaximum(10);
        jSlider8.setMinorTickSpacing(1);
        jSlider8.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider8.setPaintTicks(true);
        jSlider8.setSnapToTicks(true);
        jSlider8.setToolTipText("ER (ex: hurt = HH ER T)"); // NOI18N
        jSlider8.setValue(10);
        jSlider8.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel5.add(jSlider8);
        jSlider8.setBounds(100, 20, 31, 200);

        jSlider14.setMajorTickSpacing(10);
        jSlider14.setMaximum(10);
        jSlider14.setMinorTickSpacing(1);
        jSlider14.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider14.setPaintTicks(true);
        jSlider14.setSnapToTicks(true);
        jSlider14.setToolTipText("UH (ex: hood = HH UH D)"); // NOI18N
        jSlider14.setValue(10);
        jSlider14.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel5.add(jSlider14);
        jSlider14.setBounds(20, 20, 31, 200);

        jLabel4.setText("AY"); // NOI18N
        jPanel5.add(jLabel4);
        jLabel4.setBounds(230, 230, 20, 14);

        jLabel8.setText("ER"); // NOI18N
        jPanel5.add(jLabel8);
        jLabel8.setBounds(110, 230, 20, 14);

        jLabel14.setText("UH"); // NOI18N
        jPanel5.add(jLabel14);
        jLabel14.setBounds(30, 230, 30, 14);

        jSlider15.setMajorTickSpacing(10);
        jSlider15.setMaximum(10);
        jSlider15.setMinorTickSpacing(1);
        jSlider15.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider15.setPaintTicks(true);
        jSlider15.setSnapToTicks(true);
        jSlider15.setToolTipText("UW (ex: two = T UW)"); // NOI18N
        jSlider15.setValue(10);
        jSlider15.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel5.add(jSlider15);
        jSlider15.setBounds(140, 20, 31, 200);

        jLabel15.setText("UW"); // NOI18N
        jPanel5.add(jLabel15);
        jLabel15.setBounds(150, 230, 20, 14);

        jSlider5.setMajorTickSpacing(10);
        jSlider5.setMaximum(10);
        jSlider5.setMinorTickSpacing(1);
        jSlider5.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider5.setPaintTicks(true);
        jSlider5.setSnapToTicks(true);
        jSlider5.setToolTipText("AW (ex: cow = K AW)"); // NOI18N
        jSlider5.setValue(10);
        jSlider5.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel5.add(jSlider5);
        jSlider5.setBounds(180, 20, 31, 200);

        jLabel5.setText("AW"); // NOI18N
        jPanel5.add(jLabel5);
        jLabel5.setBounds(190, 230, 20, 14);

        jPanel1.add(jPanel5);
        jPanel5.setBounds(330, 10, 270, 260);

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Fricatives - Sibilant"));
        jPanel8.setLayout(null);

        jSlider40.setMajorTickSpacing(10);
        jSlider40.setMaximum(10);
        jSlider40.setMinorTickSpacing(1);
        jSlider40.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider40.setPaintTicks(true);
        jSlider40.setSnapToTicks(true);
        jSlider40.setToolTipText("S (ex: sea = S IY)"); // NOI18N
        jSlider40.setValue(1);
        jSlider40.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel8.add(jSlider40);
        jSlider40.setBounds(10, 20, 31, 200);

        jLabel40.setText("S"); // NOI18N
        jPanel8.add(jLabel40);
        jLabel40.setBounds(20, 230, 10, 14);

        jSlider39.setMajorTickSpacing(10);
        jSlider39.setMaximum(10);
        jSlider39.setMinorTickSpacing(1);
        jSlider39.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider39.setPaintTicks(true);
        jSlider39.setSnapToTicks(true);
        jSlider39.setToolTipText("SH (ex: she = SH IY)"); // NOI18N
        jSlider39.setValue(1);
        jSlider39.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel8.add(jSlider39);
        jSlider39.setBounds(50, 20, 31, 200);

        jLabel39.setText("SH"); // NOI18N
        jPanel8.add(jLabel39);
        jLabel39.setBounds(60, 230, 20, 14);

        jSlider33.setMajorTickSpacing(10);
        jSlider33.setMaximum(10);
        jSlider33.setMinorTickSpacing(1);
        jSlider33.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider33.setPaintTicks(true);
        jSlider33.setSnapToTicks(true);
        jSlider33.setToolTipText("Z (ex: zee = Z IY)"); // NOI18N
        jSlider33.setValue(1);
        jSlider33.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel8.add(jSlider33);
        jSlider33.setBounds(90, 20, 31, 200);

        jLabel33.setText("Z"); // NOI18N
        jPanel8.add(jLabel33);
        jLabel33.setBounds(100, 230, 10, 14);

        jSlider41.setMajorTickSpacing(10);
        jSlider41.setMaximum(10);
        jSlider41.setMinorTickSpacing(1);
        jSlider41.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider41.setPaintTicks(true);
        jSlider41.setSnapToTicks(true);
        jSlider41.setToolTipText("ZH (ex: seizure = S IY ZH ER)"); // NOI18N
        jSlider41.setValue(1);
        jSlider41.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel8.add(jSlider41);
        jSlider41.setBounds(130, 20, 31, 200);

        jLabel41.setText("ZH"); // NOI18N
        jPanel8.add(jLabel41);
        jLabel41.setBounds(140, 230, 20, 14);

        jPanel1.add(jPanel8);
        jPanel8.setBounds(440, 270, 170, 260);

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Plosive - Voiceless"));
        jPanel6.setLayout(null);

        jSlider31.setMajorTickSpacing(10);
        jSlider31.setMaximum(10);
        jSlider31.setMinorTickSpacing(1);
        jSlider31.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider31.setPaintTicks(true);
        jSlider31.setSnapToTicks(true);
        jSlider31.setToolTipText("P (ex: pee = P IY)"); // NOI18N
        jSlider31.setValue(7);
        jSlider31.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel6.add(jSlider31);
        jSlider31.setBounds(10, 20, 31, 200);

        jLabel31.setText("P"); // NOI18N
        jPanel6.add(jLabel31);
        jLabel31.setBounds(26, 230, 10, 14);

        jSlider36.setMajorTickSpacing(10);
        jSlider36.setMaximum(10);
        jSlider36.setMinorTickSpacing(1);
        jSlider36.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider36.setPaintTicks(true);
        jSlider36.setSnapToTicks(true);
        jSlider36.setToolTipText("T (ex: tea = T IY)"); // NOI18N
        jSlider36.setValue(7);
        jSlider36.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel6.add(jSlider36);
        jSlider36.setBounds(50, 20, 31, 200);

        jLabel36.setText("T"); // NOI18N
        jPanel6.add(jLabel36);
        jLabel36.setBounds(70, 230, 10, 14);

        jSlider26.setMajorTickSpacing(10);
        jSlider26.setMaximum(10);
        jSlider26.setMinorTickSpacing(1);
        jSlider26.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider26.setPaintTicks(true);
        jSlider26.setSnapToTicks(true);
        jSlider26.setToolTipText("K (ex: key = K IY)"); // NOI18N
        jSlider26.setValue(7);
        jSlider26.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel6.add(jSlider26);
        jSlider26.setBounds(90, 20, 31, 200);

        jLabel26.setText("K"); // NOI18N
        jPanel6.add(jLabel26);
        jLabel26.setBounds(106, 230, 10, 14);

        jPanel1.add(jPanel6);
        jPanel6.setBounds(310, 270, 130, 260);

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Plosives - Voiced"));
        jPanel7.setLayout(null);

        jSlider18.setMajorTickSpacing(10);
        jSlider18.setMaximum(10);
        jSlider18.setMinorTickSpacing(1);
        jSlider18.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider18.setPaintTicks(true);
        jSlider18.setSnapToTicks(true);
        jSlider18.setToolTipText("B (ex: be = B IY)"); // NOI18N
        jSlider18.setValue(3);
        jSlider18.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel7.add(jSlider18);
        jSlider18.setBounds(10, 20, 31, 200);

        jLabel18.setText("B"); // NOI18N
        jPanel7.add(jLabel18);
        jLabel18.setBounds(20, 230, 10, 14);

        jSlider20.setMajorTickSpacing(10);
        jSlider20.setMaximum(10);
        jSlider20.setMinorTickSpacing(1);
        jSlider20.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider20.setPaintTicks(true);
        jSlider20.setSnapToTicks(true);
        jSlider20.setToolTipText("D (ex: dee = D IY)"); // NOI18N
        jSlider20.setValue(3);
        jSlider20.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel7.add(jSlider20);
        jSlider20.setBounds(50, 20, 31, 200);

        jLabel20.setText("D"); // NOI18N
        jPanel7.add(jLabel20);
        jLabel20.setBounds(60, 230, 10, 14);

        jSlider21.setMajorTickSpacing(10);
        jSlider21.setMaximum(10);
        jSlider21.setMinorTickSpacing(1);
        jSlider21.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider21.setPaintTicks(true);
        jSlider21.setSnapToTicks(true);
        jSlider21.setToolTipText("G (ex: green = G R IY N)"); // NOI18N
        jSlider21.setValue(3);
        jSlider21.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel7.add(jSlider21);
        jSlider21.setBounds(90, 20, 31, 200);

        jLabel21.setText("G"); // NOI18N
        jPanel7.add(jLabel21);
        jLabel21.setBounds(100, 230, 10, 14);

        jPanel1.add(jPanel7);
        jPanel7.setBounds(180, 270, 130, 260);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Internal Rhyme"));
        jPanel4.setLayout(null);

        jSlider17.setMajorTickSpacing(10);
        jSlider17.setMaximum(10);
        jSlider17.setMinorTickSpacing(1);
        jSlider17.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider17.setPaintTicks(true);
        jSlider17.setSnapToTicks(true);
        jSlider17.setToolTipText("Assonance"); // NOI18N
        jSlider17.setValue(9);
        jSlider17.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel4.add(jSlider17);
        jSlider17.setBounds(80, 20, 31, 200);

        jSlider16.setMajorTickSpacing(10);
        jSlider16.setMaximum(10);
        jSlider16.setMinorTickSpacing(1);
        jSlider16.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider16.setPaintTicks(true);
        jSlider16.setSnapToTicks(true);
        jSlider16.setToolTipText("Alliteration"); // NOI18N
        jSlider16.setValue(10);
        jSlider16.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel4.add(jSlider16);
        jSlider16.setBounds(20, 20, 31, 200);

        jLabel16.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel16.setText("Alliteration"); // NOI18N
        jPanel4.add(jLabel16);
        jLabel16.setBounds(10, 230, 56, 13);

        jLabel17.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel17.setText("Assonance"); // NOI18N
        jPanel4.add(jLabel17);
        jLabel17.setBounds(80, 230, 48, 13);

        jCheckBox3.setText("Any");
        jCheckBox3.setToolTipText("whether 0-score phonemes count");
        jCheckBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox3ActionPerformed(evt);
            }
        });
        jPanel4.add(jCheckBox3);
        jCheckBox3.setBounds(130, 110, 50, 23);

        jPanel1.add(jPanel4);
        jPanel4.setBounds(10, 10, 200, 260);

        jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder("Front Vowels"));
        jPanel15.setLayout(null);

        jSlider11.setMajorTickSpacing(10);
        jSlider11.setMaximum(10);
        jSlider11.setMinorTickSpacing(1);
        jSlider11.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider11.setPaintTicks(true);
        jSlider11.setSnapToTicks(true);
        jSlider11.setToolTipText("IY (ex: eat = IY T)"); // NOI18N
        jSlider11.setValue(1);
        jSlider11.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel15.add(jSlider11);
        jSlider11.setBounds(20, 20, 31, 200);

        jLabel11.setText("IY"); // NOI18N
        jPanel15.add(jLabel11);
        jLabel11.setBounds(30, 230, 10, 14);

        jSlider12.setMajorTickSpacing(10);
        jSlider12.setMaximum(10);
        jSlider12.setMinorTickSpacing(1);
        jSlider12.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider12.setPaintTicks(true);
        jSlider12.setSnapToTicks(true);
        jSlider12.setToolTipText("IH (ex: it = IH T)"); // NOI18N
        jSlider12.setValue(1);
        jSlider12.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel15.add(jSlider12);
        jSlider12.setBounds(140, 20, 31, 200);

        jLabel12.setText("IH"); // NOI18N
        jPanel15.add(jLabel12);
        jLabel12.setBounds(150, 230, 11, 14);

        jSlider9.setMajorTickSpacing(10);
        jSlider9.setMaximum(10);
        jSlider9.setMinorTickSpacing(1);
        jSlider9.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider9.setPaintTicks(true);
        jSlider9.setSnapToTicks(true);
        jSlider9.setToolTipText("EY (ex: ate = EY T)"); // NOI18N
        jSlider9.setValue(1);
        jSlider9.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel15.add(jSlider9);
        jSlider9.setBounds(100, 20, 31, 200);

        jLabel9.setText("EY"); // NOI18N
        jPanel15.add(jLabel9);
        jLabel9.setBounds(110, 230, 20, 14);

        jSlider2.setMajorTickSpacing(10);
        jSlider2.setMaximum(10);
        jSlider2.setMinorTickSpacing(1);
        jSlider2.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider2.setPaintTicks(true);
        jSlider2.setSnapToTicks(true);
        jSlider2.setToolTipText("AE (ex: at = AE T)"); // NOI18N
        jSlider2.setValue(1);
        jSlider2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel15.add(jSlider2);
        jSlider2.setBounds(60, 20, 31, 200);

        jLabel2.setText("AE"); // NOI18N
        jPanel15.add(jLabel2);
        jLabel2.setBounds(70, 230, 20, 14);

        jSlider1.setMajorTickSpacing(10);
        jSlider1.setMaximum(10);
        jSlider1.setMinorTickSpacing(1);
        jSlider1.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider1.setPaintTicks(true);
        jSlider1.setSnapToTicks(true);
        jSlider1.setToolTipText("AA (ex: odd = AA D)"); // NOI18N
        jSlider1.setValue(1);
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel15.add(jSlider1);
        jSlider1.setBounds(180, 20, 31, 200);

        jLabel1.setText("AA"); // NOI18N
        jPanel15.add(jLabel1);
        jLabel1.setBounds(180, 230, 20, 14);

        jPanel1.add(jPanel15);
        jPanel15.setBounds(810, 10, 230, 260);

        jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder("Central Vowels"));
        jPanel16.setLayout(null);

        jSlider10.setMajorTickSpacing(10);
        jSlider10.setMaximum(10);
        jSlider10.setMinorTickSpacing(1);
        jSlider10.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider10.setPaintTicks(true);
        jSlider10.setSnapToTicks(true);
        jSlider10.setToolTipText("OW (ex: oat = OW T)"); // NOI18N
        jSlider10.setValue(5);
        jSlider10.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel16.add(jSlider10);
        jSlider10.setBounds(140, 20, 31, 200);

        jLabel10.setText("OW"); // NOI18N
        jPanel16.add(jLabel10);
        jLabel10.setBounds(150, 230, 30, 14);

        jSlider3.setMajorTickSpacing(10);
        jSlider3.setMaximum(10);
        jSlider3.setMinorTickSpacing(1);
        jSlider3.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider3.setPaintTicks(true);
        jSlider3.setSnapToTicks(true);
        jSlider3.setToolTipText("AH (ex: hut = HH AH T)"); // NOI18N
        jSlider3.setValue(5);
        jSlider3.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel16.add(jSlider3);
        jSlider3.setBounds(60, 20, 31, 200);

        jLabel3.setText("AH"); // NOI18N
        jPanel16.add(jLabel3);
        jLabel3.setBounds(70, 230, 20, 14);

        jSlider13.setMajorTickSpacing(10);
        jSlider13.setMaximum(10);
        jSlider13.setMinorTickSpacing(1);
        jSlider13.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider13.setPaintTicks(true);
        jSlider13.setSnapToTicks(true);
        jSlider13.setToolTipText("OY (ex: toy = T OY)"); // NOI18N
        jSlider13.setValue(5);
        jSlider13.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel16.add(jSlider13);
        jSlider13.setBounds(100, 20, 31, 200);

        jLabel13.setText("OY"); // NOI18N
        jPanel16.add(jLabel13);
        jLabel13.setBounds(110, 230, 20, 14);

        jSlider7.setMajorTickSpacing(10);
        jSlider7.setMaximum(10);
        jSlider7.setMinorTickSpacing(1);
        jSlider7.setOrientation(javax.swing.JSlider.VERTICAL);
        jSlider7.setPaintTicks(true);
        jSlider7.setSnapToTicks(true);
        jSlider7.setToolTipText("EH (ex: Ed = EH D)"); // NOI18N
        jSlider7.setValue(5);
        jSlider7.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderUpdate(evt);
            }
        });
        jPanel16.add(jSlider7);
        jSlider7.setBounds(20, 20, 31, 200);

        jLabel7.setText("EH"); // NOI18N
        jPanel16.add(jLabel7);
        jLabel7.setBounds(20, 230, 20, 14);

        jPanel1.add(jPanel16);
        jPanel16.setBounds(610, 10, 190, 260);

        jPanel17.setBorder(javax.swing.BorderFactory.createTitledBorder("Reset All"));
        jPanel17.setLayout(null);

        jButton3.setText("min"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetAllMin(evt);
            }
        });
        jPanel17.add(jButton3);
        jButton3.setBounds(10, 150, 80, 23);

        jButton4.setText("med"); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetAllMed(evt);
            }
        });
        jPanel17.add(jButton4);
        jButton4.setBounds(10, 70, 80, 23);

        jButton5.setText("max"); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetAllMax(evt);
            }
        });
        jPanel17.add(jButton5);
        jButton5.setBounds(10, 30, 80, 23);

        jButton12.setText("one");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetAllOne(evt);
            }
        });
        jPanel17.add(jButton12);
        jButton12.setBounds(10, 110, 80, 23);

        jPanel1.add(jPanel17);
        jPanel17.setBounds(220, 10, 100, 260);

        jTabbedPane1.addTab("Phonemic Evaluation", jPanel1);

        jPanel12.setLayout(null);

        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder("Language Models Available"));
        jPanel13.setLayout(null);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Shakespeare's Sonnets (Class-Based Bigram)", "Shakespeare's Sonnets (Word-Based Bigram)" }));
        jComboBox1.setSelectedIndex(1);
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeLanguageModel(evt);
            }
        });
        jPanel13.add(jComboBox1);
        jComboBox1.setBounds(20, 30, 310, 20);

        jPanel12.add(jPanel13);
        jPanel13.setBounds(10, 10, 510, 80);

        jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder("New Language Model"));
        jPanel14.setLayout(null);

        jTextArea3.setColumns(20);
        jTextArea3.setRows(5);
        jScrollPane3.setViewportView(jTextArea3);

        jPanel14.add(jScrollPane3);
        jScrollPane3.setBounds(20, 90, 470, 310);
        jPanel14.add(jTextField1);
        jTextField1.setBounds(80, 30, 300, 20);

        jLabel42.setText("Name:");
        jPanel14.add(jLabel42);
        jLabel42.setBounds(20, 30, 40, 14);

        jButton2.setText("Build");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buildNewLanguageModel(evt);
            }
        });
        jPanel14.add(jButton2);
        jButton2.setBounds(410, 30, 70, 23);

        jCheckBox4.setSelected(true);
        jCheckBox4.setText("Use Punctuation ");
        jCheckBox4.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        jCheckBox4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateUsePunctuation(evt);
            }
        });
        jPanel14.add(jCheckBox4);
        jCheckBox4.setBounds(360, 60, 130, 23);

        jPanel12.add(jPanel14);
        jPanel14.setBounds(10, 100, 510, 420);

        jPanel24.setBorder(javax.swing.BorderFactory.createTitledBorder("Show Model Details"));
        jPanel24.setLayout(null);

        jTextArea2.setBackground(new java.awt.Color(204, 204, 204));
        jTextArea2.setColumns(20);
        jTextArea2.setEditable(false);
        jTextArea2.setLineWrap(true);
        jTextArea2.setRows(5);
        jScrollPane2.setViewportView(jTextArea2);

        jPanel24.add(jScrollPane2);
        jScrollPane2.setBounds(20, 70, 460, 420);

        jButton9.setText("Overview");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showModelOverview(evt);
            }
        });
        jPanel24.add(jButton9);
        jButton9.setBounds(20, 30, 100, 23);

        jButton10.setText("Language");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showModelLanguage(evt);
            }
        });
        jPanel24.add(jButton10);
        jButton10.setBounds(140, 30, 100, 23);

        jButton11.setText("Rhyme");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showModelRhyme(evt);
            }
        });
        jPanel24.add(jButton11);
        jButton11.setBounds(390, 30, 90, 23);

        jButton13.setText("Phoneme");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showModelPronunciation(evt);
            }
        });
        jPanel24.add(jButton13);
        jButton13.setBounds(260, 30, 110, 23);

        jPanel12.add(jPanel24);
        jPanel24.setBounds(530, 10, 500, 510);

        jTabbedPane1.addTab("Modeling", jPanel12);

        getContentPane().add(jTabbedPane1);
        jTabbedPane1.setBounds(10, 10, 1060, 570);
    }// </editor-fold>//GEN-END:initComponents

    // generate a line
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        doGeneration("");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void sliderUpdate(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderUpdate
        JSlider source = (JSlider)evt.getSource();
        if (!source.getValueIsAdjusting()) {
            String sourceToolTip = source.getToolTipText();
            int value = (int)source.getValue();

            try {
                if (sourceToolTip.startsWith("Alliteration")) {
                    pe.setAlliterationWeight( value );
                } else if (sourceToolTip.startsWith("Assonance")) {
                    pe.setAssonanceWeight( value );
                } else {
                    String[] p = sourceToolTip.split(" ");
                    //String phoneme = sourceToolTip.substring(0, 2);
                    String phoneme = p[0];
                    //System.out.println( "phone is: |" + phoneme + "|");
                    pe.setPhonemesAndWeightsPairs(phoneme, value);
                }
            } catch (Exception e) {
                System.out.println("exception setting slider value");
            }
        }
    }//GEN-LAST:event_sliderUpdate

    private void resetAllMin(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetAllMin

        pe.setPhonemesAndWeightsAll(0);
        this.jSlider1.setValue(0);
        this.jSlider2.setValue(0);
        this.jSlider3.setValue(0);
        this.jSlider4.setValue(0);
        this.jSlider5.setValue(0);
        this.jSlider6.setValue(0);
        this.jSlider7.setValue(0);
        this.jSlider8.setValue(0);
        this.jSlider9.setValue(0);
        this.jSlider10.setValue(0);
        this.jSlider11.setValue(0);
        this.jSlider12.setValue(0);
        this.jSlider13.setValue(0);
        this.jSlider14.setValue(0);
        this.jSlider15.setValue(0);
        this.jSlider16.setValue(0);
        this.jSlider17.setValue(0);
        this.jSlider18.setValue(0);
        this.jSlider19.setValue(0);
        this.jSlider20.setValue(0);
        this.jSlider21.setValue(0);
        this.jSlider22.setValue(0);
        this.jSlider23.setValue(0);
        this.jSlider24.setValue(0);
        this.jSlider25.setValue(0);
        this.jSlider26.setValue(0);
        this.jSlider27.setValue(0);
        this.jSlider28.setValue(0);
        this.jSlider29.setValue(0);
        this.jSlider30.setValue(0);
        this.jSlider31.setValue(0);
        this.jSlider32.setValue(0);
        this.jSlider33.setValue(0);
        this.jSlider34.setValue(0);
        this.jSlider35.setValue(0);
        this.jSlider36.setValue(0);
        this.jSlider37.setValue(0);
        this.jSlider38.setValue(0);
        this.jSlider39.setValue(0);
        this.jSlider40.setValue(0);
        this.jSlider41.setValue(0);
    }//GEN-LAST:event_resetAllMin

    private void resetAllMed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetAllMed

        pe.setPhonemesAndWeightsAll(5);
        this.jSlider1.setValue(5);
        this.jSlider2.setValue(5);
        this.jSlider3.setValue(5);
        this.jSlider4.setValue(5);
        this.jSlider5.setValue(5);
        this.jSlider6.setValue(5);
        this.jSlider7.setValue(5);
        this.jSlider8.setValue(5);
        this.jSlider9.setValue(5);
        this.jSlider10.setValue(5);
        this.jSlider11.setValue(5);
        this.jSlider12.setValue(5);
        this.jSlider13.setValue(5);
        this.jSlider14.setValue(5);
        this.jSlider15.setValue(5);
        this.jSlider16.setValue(5);
        this.jSlider17.setValue(5);
        this.jSlider18.setValue(5);
        this.jSlider19.setValue(5);
        this.jSlider20.setValue(5);
        this.jSlider21.setValue(5);
        this.jSlider22.setValue(5);
        this.jSlider23.setValue(5);
        this.jSlider24.setValue(5);
        this.jSlider25.setValue(5);
        this.jSlider26.setValue(5);
        this.jSlider27.setValue(5);
        this.jSlider28.setValue(5);
        this.jSlider29.setValue(5);
        this.jSlider30.setValue(5);
        this.jSlider31.setValue(5);
        this.jSlider32.setValue(5);
        this.jSlider33.setValue(5);
        this.jSlider34.setValue(5);
        this.jSlider35.setValue(5);
        this.jSlider36.setValue(5);
        this.jSlider37.setValue(5);
        this.jSlider38.setValue(5);
        this.jSlider39.setValue(5);
        this.jSlider40.setValue(5);
        this.jSlider41.setValue(5);
    }//GEN-LAST:event_resetAllMed

    private void resetAllMax(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetAllMax

        pe.setPhonemesAndWeightsAll(10);
        this.jSlider1.setValue(10);
        this.jSlider2.setValue(10);
        this.jSlider3.setValue(10);
        this.jSlider4.setValue(10);
        this.jSlider5.setValue(10);
        this.jSlider6.setValue(10);
        this.jSlider7.setValue(10);
        this.jSlider8.setValue(10);
        this.jSlider9.setValue(10);
        this.jSlider10.setValue(10);
        this.jSlider11.setValue(10);
        this.jSlider12.setValue(10);
        this.jSlider13.setValue(10);
        this.jSlider14.setValue(10);
        this.jSlider15.setValue(10);
        this.jSlider16.setValue(10);
        this.jSlider17.setValue(10);
        this.jSlider18.setValue(10);
        this.jSlider19.setValue(10);
        this.jSlider20.setValue(10);
        this.jSlider21.setValue(10);
        this.jSlider22.setValue(10);
        this.jSlider23.setValue(10);
        this.jSlider24.setValue(10);
        this.jSlider25.setValue(10);
        this.jSlider26.setValue(10);
        this.jSlider27.setValue(10);
        this.jSlider28.setValue(10);
        this.jSlider29.setValue(10);
        this.jSlider30.setValue(10);
        this.jSlider31.setValue(10);
        this.jSlider32.setValue(10);
        this.jSlider33.setValue(10);
        this.jSlider34.setValue(10);
        this.jSlider35.setValue(10);
        this.jSlider36.setValue(10);
        this.jSlider37.setValue(10);
        this.jSlider38.setValue(10);
        this.jSlider39.setValue(10);
        this.jSlider40.setValue(10);
        this.jSlider41.setValue(10);
    }//GEN-LAST:event_resetAllMax

    private void phoneCheck(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phoneCheck

        String toCheck = this.jTextArea1.getSelectedText();
        if ( toCheck == null  ) {
            toCheck = this.jTextArea1.getText();
        }

        if ( toCheck != null  ) {
            toCheck = toCheck.replaceAll("\n", " ");
            PhonemeLine evaluated = pe.evaluate( toCheck );
            printDetails( "Phone Check: "  );
            printDetails( "\n  Phone counts: " + pe.phoneCheck( evaluated.getPhonemeString() ) );
            printDetails( "\n  Evaluation weights: " + pe.toString() + "\n  ");
            printDetails( evaluated.toString() + "\n\n");
        }

    }//GEN-LAST:event_phoneCheck

    private void setSearchAlgStochastic(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setSearchAlgStochastic
        //searchAlgorithm = STOCHASTIC_BEAM_SEARCH;
        gen.setSearchAlgorithmStochasticBeam();
    }//GEN-LAST:event_setSearchAlgStochastic

    private void setSearchAlgNRandom(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setSearchAlgNRandom
        gen.setSearchAlgorithmNRandom();
        //searchAlgorithm = N_RANDOM;
    }//GEN-LAST:event_setSearchAlgNRandom

    private void searchNRandPopulation(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchNRandPopulation

        JComboBox jcb = (JComboBox)evt.getSource();
        String populationSizeString = (String)jcb.getSelectedItem();
        int populationSize = Integer.parseInt( populationSizeString );
        gen.setNRandPopSize( populationSize );
    }//GEN-LAST:event_searchNRandPopulation

    private void searchStochasticBeamPopulation(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchStochasticBeamPopulation

        JComboBox jcb = (JComboBox)evt.getSource();
        String populationSizeString = (String)jcb.getSelectedItem();
        int populationSize = Integer.parseInt( populationSizeString );
        gen.setStochBeamPopSize( populationSize );

    }//GEN-LAST:event_searchStochasticBeamPopulation

    private void searchStochNumGenerations(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchStochNumGenerations
        JComboBox jcb = (JComboBox)evt.getSource();
        String generationsString = (String)jcb.getSelectedItem();
        int generations = Integer.parseInt( generationsString );
        gen.setStochBeamGenerations( generations );

    }//GEN-LAST:event_searchStochNumGenerations

    // set the number of lines to generate
    private void generationNumberLines(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generationNumberLines
        JComboBox jcb = (JComboBox)evt.getSource();
        String linesToGenerateString = (String)jcb.getSelectedItem();
        //linesToGenerate = Integer.parseInt( linesToGenerateString );
        gen.setLinesToGenerate(Integer.parseInt( linesToGenerateString ));
        //gen.linesToGenerate = Integer.parseInt( linesToGenerateString );
    }//GEN-LAST:event_generationNumberLines

    // change whether to show details or not
    private void changeStochasticShowDetails(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeStochasticShowDetails
        gen.changeShowGenerationDetails();
    }//GEN-LAST:event_changeStochasticShowDetails

    // set number of accented vowels to generate
    private void changeNumberAccentedVowels(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeNumberAccentedVowels
        JComboBox jcb = (JComboBox)evt.getSource();
        String linesToGenerateString = (String)jcb.getSelectedItem();
        gen.setNumberOfAccentedVowelPhonemes( Integer.parseInt( linesToGenerateString ) );
    }//GEN-LAST:event_changeNumberAccentedVowels

    private void changeEstimateMissing(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeEstimateMissing

        if ( gen.getEstimateMissingPhonemes() ) {
            gen.setEstimateMissingPhonemes( false );
        } else {
            gen.setEstimateMissingPhonemes( true );
        }
    }//GEN-LAST:event_changeEstimateMissing

    // find a single word that rhymes with selected text
    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed

        String insertContext = jTextArea1.getText();
        int caretPosition = jTextArea1.getCaretPosition();

        // find whether there are spaces/newlines before and after the caret
        boolean hasSpaceBefore = hasSpaceBeforeCaret( insertContext, caretPosition );
        boolean hasSpaceAfter = hasSpaceAfterCaret( insertContext, caretPosition );
        
        
        // find selected text; if not, get entire area
        String toRhyme = this.jTextArea1.getSelectedText();
        if ( toRhyme == null  ) {
            toRhyme = this.jTextArea1.getText();
            // in case the area ends with a single word after a newline - later this needs to be split by space
            toRhyme = toRhyme.replaceAll("\n", " ");
        }
        toRhyme = toRhyme.trim();

        // find word seeking a rhyme for
        String[] aA = toRhyme.split(" ");
        String wordToRhyme = aA[aA.length-1];
        printDetails("Seeking rhyme for " + wordToRhyme + "\n");

        String wordToRhymePhonemes = pe.phonemesForLine(wordToRhyme);
        String rhymeFound = rm.findRhymeFromPhonemes(wordToRhymePhonemes);
        if ( rhymeFound.equals("") ) {
            printDetails( " no rhyme found \n");
        } else {
            // put the new word into the TextArea
            StringBuffer toInsert = new StringBuffer();
            if (!hasSpaceBefore) {
                toInsert.append(" ");
            }
            toInsert.append(rhymeFound);
            if (!hasSpaceAfter) {
                toInsert.append(" ");
            }
            jTextArea1.insert(toInsert.toString(), caretPosition);
//            printOutput( rhymeFound + "\n");
            printDetails( " found rhyme: " + rhymeFound + "\n");
        }
        printDetails("\n");

        jTextArea1.requestFocusInWindow();
        
    }//GEN-LAST:event_jButton7ActionPerformed

    // find a line that rhymes with selected text
    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // find selected text; if not, get entire area
        String toRhyme = this.jTextArea1.getSelectedText();
        if ( toRhyme == null  ) {
            toRhyme = this.jTextArea1.getText();
            // in case the area ends with a single word after a newline - later this needs to be split by space
            toRhyme = toRhyme.replaceAll("\n", " ");
        }
        toRhyme = toRhyme.trim();

        // find word seeking a rhyme for
        String[] aA = toRhyme.split(" ");
        String wordToRhyme = aA[aA.length-1];
        wordToRhyme = wordToRhyme.replaceAll("\\W","");
        printDetails("Seeking line that rhymes with " + wordToRhyme + "\n");
        // see if there's a rhyme, for sole purpose of reporting failure if need be
        //String s = rm.findRhymeFromWord(wordToRhyme);
        int rhymesAvailable = rm.countRhymesAvailableForWord(wordToRhyme);

        //if ( s.equals("") ) {
        if ( rhymesAvailable == 0 ) {
            printDetails(" rhyming line not available\n\n");
        } else {
            printDetails( " " + rhymesAvailable + " rhymes available\n");
            doGeneration( wordToRhyme );
        }

//        lineFound = lineFound.trim();
//        String[] cC = lineFound.split(" ");
//        String lastWord = cC[cC.length-1];
    }//GEN-LAST:event_jButton8ActionPerformed

    // whether to use words without phoneme models
    private void jCheckBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox3ActionPerformed
        if ( pe.useAny ) {
            pe.setUseAny(false);
        } else {
            pe.setUseAny(true);
        }
    }//GEN-LAST:event_jCheckBox3ActionPerformed

    // change between language models
    private void changeLanguageModel(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeLanguageModel
        JComboBox jcb = (JComboBox)evt.getSource();
        String whichModel = (String)jcb.getSelectedItem();
        //String modelDetails = new String();
        if ( whichModel.equals(SHAKESPEARE_CLASS_BIGRAM) ) {
            if ( ! whichModelLoaded.equals(SHAKESPEARE_CLASS_BIGRAM) ) {
                // load the model
                lm = new ClassBasedBigramModel();
                readJarModelFile("../data/" + fileLocation);
                System.out.println("Loading language model");
                System.out.println(" read " + fileLocation + ", built class-based bigram language model");
                whichModelLoaded = SHAKESPEARE_CLASS_BIGRAM;
                readPhonemeAndRhymeModels();
            }
            if ( jComboBox1.getItemCount() > defaultNumberOfModels ) {
                int t = jComboBox1.getItemCount() ;
                jComboBox1.removeItemAt( defaultNumberOfModels );
            }

        } else if ( whichModel.equals( SHAKESPEARE_WORD_BIGRAM ) ) {
            if ( ! whichModelLoaded.equals(SHAKESPEARE_WORD_BIGRAM) ) {
                // load the model typeBigramFileLocation
                lm = new WordBasedBigramModel();
                readJarModelFile("../data/" + typeBigramFileLocation);
                System.out.println("Loading language model");
                System.out.println(" read " + typeBigramFileLocation + ", built type-based bigram language model");
                whichModelLoaded = SHAKESPEARE_WORD_BIGRAM;
                readPhonemeAndRhymeModels();
            }
            if ( jComboBox1.getItemCount() > defaultNumberOfModels ) {
                int t = jComboBox1.getItemCount() ;
                jComboBox1.removeItemAt( defaultNumberOfModels );
            }
        }

        // show model overview
        clearModel();
        lm.printOverviewToApplet(this);
    }//GEN-LAST:event_changeLanguageModel

    private void buildNewLanguageModel(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buildNewLanguageModel

        List inputModelList = new ArrayList();
        String inputModelText = jTextArea3.getText();
        String[] aIMT = inputModelText.split("\n");
        for (int i=0; i<aIMT.length; i++ ) {
            String currentLine = aIMT[i].trim();
            if (!currentLine.startsWith("//")) {
                inputModelList.add(currentLine);
            }
        }
        boolean b = lm.getUsePunctuation();
        lm = new WordBasedBigramModel();
        lm.setUsePunctuation(b);
        lm.readModelList(inputModelList);
        readPhonemeAndRhymeModels();

        String modelName = jTextField1.getText();
        if ( modelName.equals("") ) {
            modelName = "custom model " + userModelNumber + " (Type-Based Bigram)";
            userModelNumber++;
        }
        System.out.println("Loading language model");
        System.out.println(" read " + modelName + ", built type-based bigram language model");
        whichModelLoaded = modelName;
        jComboBox1.addItem(modelName);
        jComboBox1.setSelectedItem(modelName);

        // print out model overview
        clearModel();
        lm.printOverviewToApplet(this );

        // clear user input fields
        jTextField1.setText("");
        jTextArea3.setText("");
    }//GEN-LAST:event_buildNewLanguageModel

    // print overview of models
    private void showModelOverview(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showModelOverview
        // print out model overview
        clearModel();
        lm.printOverviewToApplet( this );

    }//GEN-LAST:event_showModelOverview

    // print language model
    private void showModelLanguage(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showModelLanguage
        // print out language model details
        clearModel();
        lm.printLanguageModelToApplet(this);
        jTextArea2.setCaretPosition( 0 );
    }//GEN-LAST:event_showModelLanguage

    // print rhyme model
    private void showModelRhyme(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showModelRhyme
        clearModel();
        rm.printRhymeModelToApplet(this);
        jTextArea2.setCaretPosition( 0 );
    }//GEN-LAST:event_showModelRhyme



    private void resetAllOne(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetAllOne

        pe.setPhonemesAndWeightsAll(1);
        this.jSlider1.setValue(1);
        this.jSlider2.setValue(1);
        this.jSlider3.setValue(1);
        this.jSlider4.setValue(1);
        this.jSlider5.setValue(1);
        this.jSlider6.setValue(1);
        this.jSlider7.setValue(1);
        this.jSlider8.setValue(1);
        this.jSlider9.setValue(1);
        this.jSlider10.setValue(1);
        this.jSlider11.setValue(1);
        this.jSlider12.setValue(1);
        this.jSlider13.setValue(1);
        this.jSlider14.setValue(1);
        this.jSlider15.setValue(1);
        this.jSlider16.setValue(1);
        this.jSlider17.setValue(1);
        this.jSlider18.setValue(1);
        this.jSlider19.setValue(1);
        this.jSlider20.setValue(1);
        this.jSlider21.setValue(1);
        this.jSlider22.setValue(1);
        this.jSlider23.setValue(1);
        this.jSlider24.setValue(1);
        this.jSlider25.setValue(1);
        this.jSlider26.setValue(1);
        this.jSlider27.setValue(1);
        this.jSlider28.setValue(1);
        this.jSlider29.setValue(1);
        this.jSlider30.setValue(1);
        this.jSlider31.setValue(1);
        this.jSlider32.setValue(1);
        this.jSlider33.setValue(1);
        this.jSlider34.setValue(1);
        this.jSlider35.setValue(1);
        this.jSlider36.setValue(1);
        this.jSlider37.setValue(1);
        this.jSlider38.setValue(1);
        this.jSlider39.setValue(1);
        this.jSlider40.setValue(1);
        this.jSlider41.setValue(1);

    }//GEN-LAST:event_resetAllOne

    private void showModelPronunciation(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showModelPronunciation

        clearModel();
        pm.printPhonemeModelToApplet(this, pe, lm);
        jTextArea2.setCaretPosition( 0 );

    }//GEN-LAST:event_showModelPronunciation

    private boolean hasSpaceBeforeCaret(String insertContext, int caretPosition) {

        char previousCharacter;
        if ( caretPosition == 0 ) {
            previousCharacter = ' ';
        } else {
            previousCharacter = insertContext.charAt(caretPosition-1);
        }
        
//        boolean toReturn = false;
        if ( previousCharacter == '\n' || previousCharacter == ' ')  {
            return true;
//            toReturn = true;
        }
        return false;
    }

    private boolean hasSpaceAfterCaret(String insertContext, int caretPosition) {
        char nextCharacter;
        if ( caretPosition == insertContext.length() ) {
            nextCharacter =  '\n';
        } else {
            nextCharacter = insertContext.charAt(caretPosition);
        }
        
//        boolean hasSpaceAfter = false;
        if ( nextCharacter == '\n' || nextCharacter == ' ' ) {
//            hasSpaceAfter = true;
            return true;
        }
        return false;
    }    

    private String[] findTokensBeforeAfter(String insertContext, int caretPosition) {
        String fullSubstringToRight = insertContext.substring(0, caretPosition );
        int previousNewline = fullSubstringToRight.lastIndexOf("\n" );
        String previousString = insertContext.substring( previousNewline+1, caretPosition ).trim();
//        System.out.println( "prevString: |" + previousString + "|" );
        
        int nextNewline = insertContext.indexOf("\n", caretPosition);
        String nextString = "";
        if ( nextNewline == -1 ) {
                nextString = insertContext.substring( caretPosition, jTextArea1.getDocument().getLength() ).trim();        
        } else if ( caretPosition != jTextArea1.getDocument().getLength() ) {
                nextString = insertContext.substring( caretPosition, nextNewline ).trim();        
        } 
//        System.out.println( "nextString: |" + nextString + "|" );
//        System.out.println();
        
        String previousToken = "";
        if ( ! previousString.equals("") ) {
            int previousSpace = previousString.lastIndexOf(" ", caretPosition);
            previousToken = previousString.substring( previousSpace+1, previousString.length() );            
        }
//        System.out.println( "previous token: |" +  previousToken  + "|" );        

        String nextToken = "";
        if ( ! nextString.equals("") ) {
            int nextSpace = nextString.indexOf(" ", 0);        
            if ( nextSpace == -1 ) {
                nextToken = nextString;
            } else {
                nextToken = nextString.substring( 0, nextSpace );
            }            
        }

        String[] toReturn = new String[2];
        toReturn[0] = previousToken;
        toReturn[1] = nextToken;
        return toReturn;
    }
    
    
    private void insertWord(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertWord

        String insertContext = jTextArea1.getText();
        int caretPosition = jTextArea1.getCaretPosition();

        // find whether there are spaces/newlines before and after the caret
        boolean hasSpaceBefore = hasSpaceBeforeCaret( insertContext, caretPosition );
        boolean hasSpaceAfter = hasSpaceAfterCaret( insertContext, caretPosition );
        
        if ( ! hasSpaceBefore && ! hasSpaceAfter ) {
            printDetails("cannot insert word into existing word\n\n");
            return;
        }

        // find tokens before and after the caret
        String[] tokenBeforeAfter = findTokensBeforeAfter(insertContext, caretPosition);
        String previousToken = tokenBeforeAfter[0];
        String nextToken = tokenBeforeAfter[1];


        // find word to insert
        String wordToInsert = lm.generateWord( previousToken, nextToken );


        // TO DO: MAKE SURE THIS ISNT PUNCTUATION
        // put the new word into the TextArea
        StringBuffer toInsert = new StringBuffer();
        if ( ! hasSpaceBefore && ! wordToInsert.matches(".*PUNCTUATION.*") ) {
            toInsert.append(" ");
        }


        wordToInsert = wordToInsert.replaceAll("PERIODPUNCTUATION", ".");
        wordToInsert = wordToInsert.replaceAll("QUESTIONMARKPUNCTUATION", "?");
        wordToInsert = wordToInsert.replaceAll("EXCLAMATIONMARKPUNCTUATION", "!");
        wordToInsert = wordToInsert.replaceAll("COMMAPUNCTUATION", ",");
        wordToInsert = wordToInsert.replaceAll("SEMICOLONPUNCTUATION", ";");
        wordToInsert = wordToInsert.replaceAll("COLONPUNCTUATION", ":");

        
        toInsert.append( wordToInsert);
        if ( ! hasSpaceAfter ) {
            toInsert.append(" ");
        }
        jTextArea1.insert(toInsert.toString(), caretPosition);        
        jTextArea1.requestFocusInWindow();
    }//GEN-LAST:event_insertWord

    private void findNextWords(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findNextWords

        String insertContext = jTextArea1.getText();
        int caretPosition = jTextArea1.getCaretPosition();

        // find tokens before and after the caret
        String[] tokenBeforeAfter = findTokensBeforeAfter(insertContext, caretPosition);
        String previousToken = tokenBeforeAfter[0];
//        String nextToken = tokenBeforeAfter[1];

        printDetails("Next words for \"" + previousToken + "\":\n");
        lm.printNextWordsToApplet(this, previousToken);
        printDetails("\n");        
        
        
        jTextArea1.requestFocusInWindow();        
    }//GEN-LAST:event_findNextWords

    private void findtPrevWords(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findtPrevWords

        String insertContext = jTextArea1.getText();
        int caretPosition = jTextArea1.getCaretPosition();

        // find tokens before and after the caret
        String[] tokenBeforeAfter = findTokensBeforeAfter(insertContext, caretPosition);
//        String previousToken = tokenBeforeAfter[0];
        String nextToken = tokenBeforeAfter[1];

        printDetails("Previous words for \"" + nextToken + "\":\n");
        lm.printPreviousWordsToApplet(this, nextToken);
        printDetails("\n");        
               
        jTextArea1.requestFocusInWindow();        
}//GEN-LAST:event_findtPrevWords

    private void updateRhymeScheme(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateRhymeScheme

        JComboBox jcb = (JComboBox)evt.getSource();
        String stringRhymeScheme = (String)jcb.getSelectedItem();
        gen.setRhymeScheme( stringRhymeScheme );

    }//GEN-LAST:event_updateRhymeScheme

    private void updateUsePunctuation(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateUsePunctuation

        lm.changeUsePunctuation();

    }//GEN-LAST:event_updateUsePunctuation

    private void setEnjambment(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setEnjambment

        gen.switchUseEnjambment();
    }//GEN-LAST:event_setEnjambment

    private void switchTrailingNewline(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_switchTrailingNewline
        // TODO add your handling code here:
        gen.switchTrailingNewline();

    }//GEN-LAST:event_switchTrailingNewline




    // append to output
    public void printOutput( String details ){
        jTextArea1.append(details);
        jTextArea1.setCaretPosition( jTextArea1.getDocument().getLength());
    }

    // append to output
    public void printDetails( String details ){
        jTextArea4.append(details);
        jTextArea4.setCaretPosition( jTextArea4.getDocument().getLength());
    }

    // append to model window
    public void printModel( String details ){
        jTextArea2.append(details);
        //jTextArea2.setCaretPosition( jTextArea2.getDocument().getLength());
    }

    // empty the model window
    public void clearModel() {
        jTextArea2.setText("");
    }

    // generate line(s)
    public void doGeneration( String wordToRhyme ) {

        if ( ! isLoaded ) {
            setup();
        }

        gen.generateVerse(this, wordToRhyme );

//        if (searchAlgorithm.equals(STOCHASTIC_BEAM_SEARCH)) {
//            gen.generateVerse(this, wordToRhyme, true);
//        } else {
//            gen.generateVerse(this, wordToRhyme, false);
//        }

//        String[] generatedOutput;

        // TO DO: move to generation
//        if ( true ) {
//
//        } else {
//            for (int x = 1; x <= linesToGenerate; x++) {
//
//                if (searchAlgorithm.equals(STOCHASTIC_BEAM_SEARCH)) {
//
//                    System.out.println("\n* * * * * * * * * * * * * * \n");
//                    System.out.println("LINE: " + x);
//
//                    generatedOutput = gen.generateStochasticBeamSearch(wordToRhyme);
//                } else {
//                    generatedOutput = gen.generateNRandom(wordToRhyme);
//                }
//
//                printDetails(generatedOutput[0]);
//                printOutput(generatedOutput[1]);
//            }
//        }

    }



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBox jCheckBox6;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JComboBox jComboBox4;
    private javax.swing.JComboBox jComboBox5;
    private javax.swing.JComboBox jComboBox6;
    private javax.swing.JComboBox jComboBox7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JSlider jSlider10;
    private javax.swing.JSlider jSlider11;
    private javax.swing.JSlider jSlider12;
    private javax.swing.JSlider jSlider13;
    private javax.swing.JSlider jSlider14;
    private javax.swing.JSlider jSlider15;
    private javax.swing.JSlider jSlider16;
    private javax.swing.JSlider jSlider17;
    private javax.swing.JSlider jSlider18;
    private javax.swing.JSlider jSlider19;
    private javax.swing.JSlider jSlider2;
    private javax.swing.JSlider jSlider20;
    private javax.swing.JSlider jSlider21;
    private javax.swing.JSlider jSlider22;
    private javax.swing.JSlider jSlider23;
    private javax.swing.JSlider jSlider24;
    private javax.swing.JSlider jSlider25;
    private javax.swing.JSlider jSlider26;
    private javax.swing.JSlider jSlider27;
    private javax.swing.JSlider jSlider28;
    private javax.swing.JSlider jSlider29;
    private javax.swing.JSlider jSlider3;
    private javax.swing.JSlider jSlider30;
    private javax.swing.JSlider jSlider31;
    private javax.swing.JSlider jSlider32;
    private javax.swing.JSlider jSlider33;
    private javax.swing.JSlider jSlider34;
    private javax.swing.JSlider jSlider35;
    private javax.swing.JSlider jSlider36;
    private javax.swing.JSlider jSlider37;
    private javax.swing.JSlider jSlider38;
    private javax.swing.JSlider jSlider39;
    private javax.swing.JSlider jSlider4;
    private javax.swing.JSlider jSlider40;
    private javax.swing.JSlider jSlider41;
    private javax.swing.JSlider jSlider5;
    private javax.swing.JSlider jSlider6;
    private javax.swing.JSlider jSlider7;
    private javax.swing.JSlider jSlider8;
    private javax.swing.JSlider jSlider9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextArea jTextArea3;
    private javax.swing.JTextArea jTextArea4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.ButtonGroup searchButtonGroup1;
    // End of variables declaration//GEN-END:variables

    // define phoneme constants
    static final String AA = "AA";
    static final String AE = "AE";
    static final String AH = "AH";
    static final String AO = "AO";
    static final String AW = "AW";
    static final String AY = "AY";
    static final String B = "B";
    static final String CH = "CH";
    static final String D = "D";
    static final String DH = "DH";
    static final String EH = "EH";
    static final String ER = "ER";
    static final String EY = "EY";
    static final String F = "F";
    static final String G = "G";
    static final String HH = "HH";
    static final String IH = "IH";
    static final String IY = "IY";
    static final String JH = "JH";
    static final String K = "K";
    static final String L = "L";
    static final String M = "M";
    static final String N = "N";
    static final String NG = "NG";
    static final String OW = "OW";
    static final String OY = "OY";
    static final String P = "P";
    static final String R = "R";
    static final String S = "S";
    static final String SH = "SH";
    static final String T = "T";
    static final String TH = "TH";
    static final String UH = "UH";
    static final String UW = "UW";
    static final String V = "V";
    static final String W = "W";
    static final String Y = "Y";
    static final String Z = "Z";
    static final String ZH = "ZH";

}
