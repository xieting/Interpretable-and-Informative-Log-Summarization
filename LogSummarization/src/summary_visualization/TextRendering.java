package summary_visualization;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
 
public class TextRendering extends JScrollPane{
   
	/**
	 * 
	 */
	private static final long serialVersionUID = -5196566180044198657L;
   
   public TextRendering(JTextPane textArea,ArrayList<String> content,ArrayList<Float> opacities,Font f){
	   super(textArea);
	   appendToPane(textArea, content.get(0),new Color(0f, 0f, 0f, opacities.get(0)),f);
	   
	    for (int i=1;i<content.size();i++){
	    	appendToPane(textArea,", "+content.get(i),new Color(0f, 0f, 0f, opacities.get(i)),f);
	    }	    
		textArea.setEditable(false);
   }
  
   private void appendToPane(JTextPane tp, String msg, Color c,Font f)
   {
       StyleContext sc = StyleContext.getDefaultStyleContext();
       AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

       aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
       aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
       
       int len = tp.getDocument().getLength();
       tp.setFont(f);
       tp.setCaretPosition(len);
       tp.setCharacterAttributes(aset, false);
       tp.replaceSelection(msg);
   }
  
 
  public static JPanel createMainPanel(ArrayList<String> sections, List<ArrayList<String>> sectionContents,List<ArrayList<Float>> sectionOpacities) {
    int sectionSize=sections.size();

    JPanel mainPanel = new JPanel();
    GridBagLayout gblayout=new GridBagLayout();
    mainPanel.setLayout(gblayout);
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
	
    TextRendering textcontent;
    ArrayList<String> content;
    ArrayList<Float> opacities;
    
    Font f = new Font(Font.SANS_SERIF, 3, 30);
    
    for(int i=0;i<sectionSize;i++){
    	
    	JLabel label=new JLabel(sections.get(i));
    	label.setForeground(new Color(0f, 0f, 0f, 1f));
    	label.setFont(f);
    	c.fill = GridBagConstraints.BOTH;
    	c.weightx=0;
    	c.gridx = 0;
    	c.gridy = i;
        mainPanel.add(label,c);
    	 
    	 content=sectionContents.get(i);
    	 opacities=sectionOpacities.get(i);
    	 JTextPane textArea=new JTextPane();
    	 textcontent=new TextRendering(textArea,content,opacities,f);
     	c.weightx=0.3;  
     	c.ipady=150;
        c.fill = GridBagConstraints.BOTH;
    	c.gridx = 1;
    	c.gridy = i;
    	 mainPanel.add(textcontent,c);
    } 
    
    return mainPanel;   
  }
}
