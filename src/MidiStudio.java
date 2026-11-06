import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
public class MidiStudio {
    static final int[] NOTES={36,38,42,46,49,51,39,56};
    static final String[] NAMES={"Kick","Snare","Closed hat","Open hat","Crash","Ride","Clap","Cowbell"};
    static Sequence sequence(boolean[][]steps,int bpm)throws InvalidMidiDataException{
        if(steps.length!=8||bpm<40||bpm>240)throw new IllegalArgumentException("Use 8 rows and tempo 40–240");
        Sequence sequence=new Sequence(Sequence.PPQ,96);Track track=sequence.createTrack();int micros=60000000/bpm;
        MetaMessage tempo=new MetaMessage();tempo.setMessage(0x51,new byte[]{(byte)(micros>>16),(byte)(micros>>8),(byte)micros},3);track.add(new MidiEvent(tempo,0));
        for(int row=0;row<8;row++){if(steps[row].length!=16)throw new IllegalArgumentException("Each row needs 16 steps");for(int col=0;col<16;col++)if(steps[row][col]){
            ShortMessage on=new ShortMessage();on.setMessage(ShortMessage.NOTE_ON,9,NOTES[row],100);ShortMessage off=new ShortMessage();off.setMessage(ShortMessage.NOTE_OFF,9,NOTES[row],0);track.add(new MidiEvent(on,col*24L));track.add(new MidiEvent(off,col*24L+12));
        }}MetaMessage end=new MetaMessage();end.setMessage(0x2F,new byte[0],0);track.add(new MidiEvent(end,384));return sequence;
    }
    static String encode(boolean[][]steps){StringBuilder s=new StringBuilder();for(boolean[]row:steps)for(boolean v:row)s.append(v?'1':'0');return s.toString();}
    static boolean[][]decode(String text){if(!text.matches("[01]{128}"))throw new IllegalArgumentException("Invalid 8×16 pattern");boolean[][]p=new boolean[8][16];for(int i=0;i<128;i++)p[i/16][i%16]=text.charAt(i)=='1';return p;}
    static void tests()throws Exception{
        boolean[][]p=new boolean[8][16];p[0][0]=true;p[1][4]=true;Sequence s=sequence(p,120);int on=0,off=0;for(int i=0;i<s.getTracks()[0].size();i++){MidiMessage m=s.getTracks()[0].get(i).getMessage();if(m instanceof ShortMessage sm){if(sm.getCommand()==ShortMessage.NOTE_ON)on++;if(sm.getCommand()==ShortMessage.NOTE_OFF)off++;}}
        if(on!=2||off!=2||s.getTickLength()!=384||!encode(decode(encode(p))).equals(encode(p)))throw new AssertionError();ByteArrayOutputStream bytes=new ByteArrayOutputStream();MidiSystem.write(s,1,bytes);if(MidiSystem.getSequence(new ByteArrayInputStream(bytes.toByteArray())).getTickLength()!=384)throw new AssertionError();System.out.println("MIDI notes, duration, pattern validation, and MIDI round trip passed");
    }
    static void ui(){
        JFrame frame=new JFrame("MIDI Pattern Studio");frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);frame.setSize(1050,540);JPanel root=new JPanel(new BorderLayout(12,12));root.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));frame.setContentPane(root);
        JCheckBox[][]cells=new JCheckBox[8][16];JPanel grid=new JPanel(new GridLayout(9,17));grid.add(new JLabel("DRUMS"));for(int c=0;c<16;c++)grid.add(new JLabel(Integer.toString(c+1),SwingConstants.CENTER));
        for(int r=0;r<8;r++){grid.add(new JLabel(NAMES[r]));for(int c=0;c<16;c++){cells[r][c]=new JCheckBox();cells[r][c].setHorizontalAlignment(SwingConstants.CENTER);cells[r][c].setOpaque(true);cells[r][c].setBackground(c%4==0?new Color(210,235,222):Color.WHITE);grid.add(cells[r][c]);}}root.add(grid,BorderLayout.CENTER);
        JSpinner bpm=new JSpinner(new SpinnerNumberModel(110,40,240,1));JPanel controls=new JPanel();controls.add(new JLabel("BPM"));controls.add(bpm);root.add(controls,BorderLayout.SOUTH);JLabel status=new JLabel("Compose one bar of 16th notes. Playback availability depends on your MIDI synthesizer.");root.add(status,BorderLayout.NORTH);
        Sequencer[]player={null};java.util.function.Supplier<boolean[][]>pattern=()->{boolean[][]p=new boolean[8][16];for(int r=0;r<8;r++)for(int c=0;c<16;c++)p[r][c]=cells[r][c].isSelected();return p;};Runnable stop=()->{if(player[0]!=null){player[0].stop();player[0].close();player[0]=null;}status.setText("Stopped.");};
        java.util.function.BiConsumer<String,Runnable>button=(name,action)->{JButton b=new JButton(name);controls.add(b);b.addActionListener(e->{try{action.run();}catch(RuntimeException error){JOptionPane.showMessageDialog(frame,error.getMessage());}});};
        button.accept("Play loop",()->{stop.run();try{player[0]=MidiSystem.getSequencer();player[0].open();player[0].setSequence(sequence(pattern.get(),(Integer)bpm.getValue()));player[0].setLoopCount(Sequencer.LOOP_CONTINUOUSLY);player[0].setLoopStartPoint(0);player[0].setLoopEndPoint(383);player[0].start();status.setText("Playing snapshot. Stop/play to hear pattern or tempo edits.");}catch(Exception e){stop.run();throw new IllegalStateException("MIDI playback unavailable: "+e.getMessage());}});
        button.accept("Stop",stop);button.accept("Clear",()->{for(var row:cells)for(var cell:row)cell.setSelected(false);});
        button.accept("Export MIDI",()->{JFileChooser chooser=new JFileChooser();if(chooser.showSaveDialog(frame)==JFileChooser.APPROVE_OPTION)try{if(MidiSystem.write(sequence(pattern.get(),(Integer)bpm.getValue()),1,chooser.getSelectedFile())==0)throw new IOException("No MIDI writer");}catch(Exception e){throw new IllegalStateException(e);}});
        button.accept("Save pattern",()->{JFileChooser chooser=new JFileChooser();if(chooser.showSaveDialog(frame)==JFileChooser.APPROVE_OPTION)try{Properties p=new Properties();p.setProperty("bpm",bpm.getValue().toString());p.setProperty("steps",encode(pattern.get()));try(Writer out=Files.newBufferedWriter(chooser.getSelectedFile().toPath())){p.store(out,"MIDI Pattern Studio");}}catch(IOException e){throw new IllegalStateException(e);}});
        button.accept("Open pattern",()->{JFileChooser chooser=new JFileChooser();if(chooser.showOpenDialog(frame)==JFileChooser.APPROVE_OPTION)try{Properties p=new Properties();try(Reader in=Files.newBufferedReader(chooser.getSelectedFile().toPath())){p.load(in);}int tempo=Integer.parseInt(p.getProperty("bpm"));if(tempo<40||tempo>240)throw new IllegalArgumentException("Invalid tempo");var loaded=decode(p.getProperty("steps",""));stop.run();bpm.setValue(tempo);for(int r=0;r<8;r++)for(int c=0;c<16;c++)cells[r][c].setSelected(loaded[r][c]);}catch(IOException e){throw new IllegalStateException(e);}});
        frame.addWindowListener(new java.awt.event.WindowAdapter(){public void windowClosed(java.awt.event.WindowEvent e){stop.run();}});frame.setLocationRelativeTo(null);frame.setVisible(true);
    }
    public static void main(String[]args)throws Exception{if(args.length>0&&args[0].equals("--self-test")){tests();return;}SwingUtilities.invokeLater(MidiStudio::ui);}
}
