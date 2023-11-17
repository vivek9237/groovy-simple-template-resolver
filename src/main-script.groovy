import javax.swing.JWindow;
import java.awt.Toolkit;

def splashImage = new ImageIcon("images/splash-dark.jpg")
// Create the splash window
def splash = new JWindow()
def splashLabel = new JLabel(splashImage)
splash.add(splashLabel)
// Get screen dimensions
def screenSize = Toolkit.getDefaultToolkit().getScreenSize()
def imageWidth = splashImage.getIconWidth()
def imageHeight = splashImage.getIconHeight()
// Position the splash screen in the center of the screen
splash.setBounds(((screenSize.width - imageWidth)*0.5) as int, ((screenSize.height - imageHeight)*0.5) as int, imageWidth as int, imageHeight as int)
splash.setVisible(true)

import bindingConfigs.internalBindingClasses.*;
import javax.swing.* 
import groovy.swing.SwingBuilder
import groovy.text.SimpleTemplateEngine
import groovy.text.Template
import java.util.Iterator
import javax.swing.undo.UndoManager
import java.awt.Frame
import java.awt.Color
import java.awt.Font
import java.awt.GridBagLayout
import java.awt.GridBagConstraints
import groovy.json.JsonOutput
import groovy.json.JsonBuilder
import javax.swing.text.DefaultCaret
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.Insets
import java.util.List
import java.util.ArrayList
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.ActionEvent
import java.awt.event.InputEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.Desktop
import java.net.URI
import java.util.Base64
import java.net.URLEncoder
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import java.util.Random
import javax.swing.border.LineBorder
import javax.swing.UIManager
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import org.apache.groovy.json.internal.LazyMap

import javax.swing.tree.*
import java.awt.Component
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor

import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;


UIManager.put("Button.select", new Color(40, 40, 40)) // Background color when button is pressed
UIManager.put("Button.focus", Color.GRAY) // Focus color
UIManager.put("OptionPane.messageFont", new Font("Dialog", Font.PLAIN, 18));


beta_feature = false
if (args.length > 0) {
    def beta = args[0]
    if(beta.equalsIgnoreCase("beta")){
        beta_feature = true
    }
}
size_factor = 1.5;
if (screenSize.width < 900) {
    size_factor = 0.9
} else if (screenSize.width < 1280) {
    size_factor = 1
} else if (screenSize.width < 1600) {
    size_factor = 1.2
} else {
    size_factor = 1.5
}
core_binding_manipulation_file = 'bindingConfigs/sav-core-binding.groovy';
bindingJson = "bindingConfigs/binding.json";
bindingVariableMap = ['':'']
defaultBindingMapGlobal = [:]
def resolveButton
def comboBoxModel = new DefaultComboBoxModel()
def listOfBindingKeys = []
def listOfBindingValues = []


//println('Hello World!')

def newVariablePopUp = new SwingBuilder();
newVariablePopUpFrame = newVariablePopUp.frame(title: 'Add/Edit Variables in binding.json', size: [screenSize.width*0.4 as int, screenSize.height*0.4 as int],resizable:false,alwaysOnTop:true) {
    def subPanel = panel(preferredSize: [screenSize.width*0.4 as int, screenSize.height*0.4 as int],background: new Color(40, 40, 40)) {
        vbox{
            def font = new Font('Sans Serif', Font.BOLD, 14*size_factor)
            hbox{
                label('     ')
            }
            hbox{
                label('Name:',font: font, foreground: Color.WHITE)
                newVariableBindingVariableName = textField(font: font, preferredSize: [screenSize.width*0.4*0.7 as int, screenSize.height*0.4*0.1 as int])
            }
            hbox{
                label('     ')
            }
            hbox{
                label('Value:',font: font, foreground: Color.WHITE,)
                newVariableBindingVariableValue = textField(font: font, preferredSize: [screenSize.width*0.4*0.7 as int, screenSize.height*0.4*0.1 as int])
            }
            hbox{
                label('     ')
            }
            hbox{
                button(text: 'Add Variable',font: font,preferredSize: [screenSize.width*0.4*0.3 as int, screenSize.height*0.4*0.2 as int], actionPerformed: {
                    addOrUpdateBindingVariable(newVariableBindingVariableName.text,newVariableBindingVariableValue.text)
                    newVariableBindingVariableName.text = ""
                    newVariableBindingVariableValue.text = ""
                    newVariablePopUpFrame.dispose()
                    resolveButton.doClick();
                })
            }
            hbox{
                label('     ')
            }
            hbox{
                label(text:"Note: This will add or edit binding.json permanently.",foreground: Color.RED,font: new Font('Arial', Font.BOLD, 10*size_factor))
            }
            hbox{
                label('     ')
            }
        }
    }
}

def updateVariablePopUp = new SwingBuilder()
updateVariablePopUpFrame = updateVariablePopUp.frame(title: 'Update Existing Variables (Temporary)', size: [screenSize.width*0.4 as int, screenSize.height*0.4 as int],resizable:true,alwaysOnTop:true) {
    def subPanel = panel(preferredSize: [screenSize.width*0.4 as int, screenSize.height*0.4 as int],background: new Color(40, 40, 40)) {
        def font = new Font('Sans Serif', Font.BOLD, 14*size_factor)
        vbox{

            hbox{
                label('     ')
            }
            hbox{
                label('     ')
            }
            hbox{
                label('Name:',font: font, foreground: Color.WHITE,)
                
                updateBindingVariableName = comboBox(id: 'searchableComboBox', model: comboBoxModel, editable: true,font: font,preferredSize: [screenSize.width*0.4*0.7 as int, screenSize.height*0.4*0.1 as int],background: Color.BLACK, foreground: Color.WHITE,maximumRowCount: 5)
                // Adding KeyListener to filter comboBox items
                def bindingVariableList = buildDefaultBindingVariables().keySet() as ArrayList
                bindingVariableList.each{
                    comboBoxModel.addElement(it)
                }
                updateBindingVariableName.selectedItem = null
                updateBindingVariableName.editor.editorComponent.addKeyListener(new KeyAdapter() {
                    void keyReleased(KeyEvent e) {
                        def text = updateBindingVariableName.editor.item
                        comboBoxModel.removeAllElements()
                        def comboBoxSet = [] as Set
                        if(text == null || text.equalsIgnoreCase("")){
                            def bindingVariableListNew = buildDefaultBindingVariables().keySet() as ArrayList
                            bindingVariableListNew.each{
                                comboBoxModel.addElement(it)
                            }
                            updateBindingVariableName.selectedItem = null
                        } else{
							this.listOfBindingKeys.each {
								if (it.toLowerCase().startsWith(text.toLowerCase()) && text.size()>0) {
									variableName = it.split(" : ")[0]
									if(text.count(".")==0){
										variableName = variableName.split("\\.")[0]
									} else if(text.count(".")==1){
										variableName = variableName.split("\\.")[0] + "." +variableName.split("\\.")[1]
									} else if(text.count(".")==2){
										variableName = variableName.split("\\.")[0] + "." +variableName.split("\\.")[1] + "." +variableName.split("\\.")[2]
									} else if(text.count(".")==3){
										variableName = variableName.split("\\.")[0] + "." +variableName.split("\\.")[1] + "." +variableName.split("\\.")[2] + "." +variableName.split("\\.")[3]
									} else if(text.count(".")==4){
										variableName = variableName.split("\\.")[0] + "." +variableName.split("\\.")[1] + "." +variableName.split("\\.")[2] + "." +variableName.split("\\.")[3] + "." +variableName.split("\\.")[4]
									} else if(text.count(".")==5){
										variableName = variableName.split("\\.")[0] + "." +variableName.split("\\.")[1] + "." +variableName.split("\\.")[2] + "." +variableName.split("\\.")[3] + "." +variableName.split("\\.")[4] + "." +variableName.split("\\.")[5]
									}
									comboBoxSet.add(variableName)
									//bindingVariableValue.text = it.split(" : ")[1]
								}
							}
							for (dropdownItem in comboBoxSet.toList().sort()) {
								comboBoxModel.addElement(dropdownItem)
							}
							updateBindingVariableName.showPopup()
							if (!text.isEmpty()) {
								updateBindingVariableName.editor.item = text
							}
                        }
                        
                    }
                })
                //bindingVariableName = textField(font: new Font('Courier New', Font.PLAIN, 14*size_factor), preferredSize: [300, 45])
            }
            hbox{
                label('     ')
            }
            hbox{
                label('     ')
            }
            hbox{
                label('Value:',font: font, foreground: Color.WHITE)
                updateBindingVariableValue = textField(font: font, preferredSize: [screenSize.width*0.4*0.7 as int, screenSize.height*0.4*0.1 as int])
            }
            hbox{
                label('     ')
            }
            hbox{
                label('     ')
            }
            hbox{
                button(text: 'Update Variable',font: font,preferredSize: [screenSize.width*0.4*0.3 as int, screenSize.height*0.4*0.2 as int],  actionPerformed: {
                    try{
                        updateExistingBindingVariable(updateBindingVariableName.selectedItem,updateBindingVariableValue.text)
                        //bindingVariableName.text = ""
                        updateBindingVariableName.model.removeAllElements()
                        def bindingVariableListNew = buildDefaultBindingVariables().keySet() as ArrayList
                        bindingVariableListNew.each{
                            updateBindingVariableName.model.addElement(it)
                        }
                        updateBindingVariableName.selectedItem = null;
                        updateBindingVariableValue.text = "";
                        updateVariablePopUpFrame.dispose();
                        resolveButton.doClick();
                    } catch(Exception e){
                        labelNote.text = e.getMessage();
                        //e.printStackTrace();
                    }
                })
            }
            hbox{
                label('     ')
            }
            hbox{
                label('     ')
            }
            hbox{
                labelNote = label(text:"Note: These are temporary changes and will be lost if you restart this tool",foreground: Color.RED,font: new Font('Arial', Font.BOLD, 10*size_factor))
            }
            hbox{
                label('     ')
            }
        }
    }
}

def mainSwing = new SwingBuilder()
myFrame = mainSwing.frame(title: 'Groovy Script Resolver', size: [screenSize.width as int, screenSize.height as int],defaultCloseOperation: javax.swing.WindowConstants.EXIT_ON_CLOSE,iconImage: new ImageIcon("images/icon.jpg").image) {
    menuBar {
        def font = new Font('Dialog', Font.PLAIN, 18)
        settingsMenu = menu(text: ' Settings ',font: font,size: [screenSize.width/20 as int, screenSize.height as int]) {
            wordWrap = checkBoxMenuItem(text: '   Word wrap', font: font,selected: false,actionPerformed: {
                    outputTextArea.wrapStyleWord = wordWrap.selected
                    outputTextArea.lineWrap = wordWrap.selected
                    inputTextArea.wrapStyleWord = wordWrap.selected
                    inputTextArea.lineWrap = wordWrap.selected
                }
            )
            groovyTheme = checkBoxMenuItem(text: '   Groovy theme (WIP)', font: font,selected: true, enabled:false,actionPerformed: {
                    //println(groovyTheme.selected)
                    if(groovyTheme.selected){
                        Theme darkTheme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/eclipese.xml"))
                        darkTheme.apply(inputTextArea)
                        inputTextArea.setBackground(Color.BLACK)
                        Theme darkTheme1 = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/monokai.xml"))
                        darkTheme1.apply(outputTextArea)
                        outputTextArea.setBackground(Color.BLACK)
                        
                    } else{
                        inputTextArea.foreground=Color.WHITE
                        outputTextArea.foreground=Color.WHITE
                        //println(inputTextArea.foreground)
                    }
                }
            )
        }
        externalToolsMenu = menu(text: ' External tools ',font: font,size: [screenSize.width/20 as int, screenSize.height as int]) {
            jsonValidator = menuItem(text: '   JSON validator',font: font, actionPerformed: {
                if (Desktop.isDesktopSupported()) {
                    def desktop = Desktop.getDesktop()
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        def originalString = outputTextArea.text
                        def base64String = Base64.getEncoder().encodeToString(originalString.bytes)
                        def urlEncodedBase64 = URLEncoder.encode(base64String, "UTF-8")
                        desktop.browse(new URI("https://vivek9237.github.io/json-validator"))
                    }
                }
            })
        }
        helpMenu = menu(text: ' Help ',font: font,size: [screenSize.width/20 as int, screenSize.height as int]) {
            welcome = menuItem(text: '   Welcome',font: font, actionPerformed: {
                if (Desktop.isDesktopSupported()) {
                    def desktop = Desktop.getDesktop()
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        desktop.browse(new URI("https://saviyntars.atlassian.net/wiki/spaces/SSM/pages/4465131567/groovy-template-resolver"))
                    }
                }
            })
            videoTutorial = menuItem(text: '   Video tutorial',font: font, actionPerformed: {
                if (Desktop.isDesktopSupported()) {
                    def desktop = Desktop.getDesktop()
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        desktop.browse(new URI(""))
                    }
                }
            })
            separator()
            showBinding = menuItem(text: '   Show all binding variables',font: font, actionPerformed: {
                try{
                    Map bindingMapAll = buildDefaultBindingVariables()
                    def descriptionJson = new JsonBuilder( bindingMapAll ).toPrettyString()
                    createAndShowGUI(descriptionJson)
				} catch(Exception e){
                    outputTextArea.foreground = Color.RED
					outputTextArea.text = e.getMessage();
				}
            })
            separator()
            about = menuItem(text: '   About',font: font, actionPerformed: {
                JOptionPane.showMessageDialog(myFrame, "<html>Version: 3.1<br>Date: 31st Oct 2023<br>Author: Saviynt PM Team</html>");
            })
        }
    }
    panel(background: new Color(40, 40, 40)) {
        int widthTextArea = screenSize.width*0.42
        int heightTextArea = screenSize.height*0.88
        inputScrollPane = scrollPane(preferredSize: [widthTextArea as int, heightTextArea as int]) {
            if(beta_feature){
                inputTextArea = textPane(id: 'input', background: Color.BLACK, foreground: Color.WHITE, caretColor: Color.WHITE, font: new Font('Courier New', Font.PLAIN, 14*size_factor))
            } else{
                //inputTextArea = textArea(id: 'input', columns: 60, rows: 30, lineWrap:false, wrapStyleWord:false, background: Color.BLACK, foreground: Color.WHITE, caretColor: Color.WHITE, font: new Font('Courier New', Font.PLAIN, 14*size_factor))
                //inputTextArea =rSyntaxTextArea(id: 'input', columns: 60, rows: 30, lineWrap:false, wrapStyleWord:false, background: Color.BLACK, foreground: Color.WHITE, caretColor: Color.WHITE, font: new Font('Courier New', Font.PLAIN, 14*size_factor))
                inputTextArea = widget(new RSyntaxTextArea(syntaxEditingStyle: RSyntaxTextArea.SYNTAX_STYLE_GROOVY,codeFoldingEnabled:true,antiAliasingEnabled:true,font: new Font('Courier New', Font.PLAIN, 14*size_factor)))
                Theme darkTheme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/monokai.xml"))
                darkTheme.apply(inputTextArea)
                inputTextArea.setFont(new Font('Courier New', Font.PLAIN, 14*size_factor))
                inputTextArea.setBackground(Color.BLACK)
            }
            def undoManager = new UndoManager()
            inputTextArea.getDocument().addUndoableEditListener(undoManager)
            inputTextArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "Undo")
            inputTextArea.getActionMap().put("Undo", new AbstractAction() {
                @Override
                void actionPerformed(ActionEvent e) {
                    if (undoManager.canUndo()) {
                        undoManager.undo()
                    }
                }
            })
            inputTextArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "Redo")
            inputTextArea.getActionMap().put("Redo", new AbstractAction() {
                @Override
                void actionPerformed(ActionEvent e) {
                    if (undoManager.canRedo()) {
                        undoManager.redo()
                    }
                }
            })
            // Create a popup menu
            JPopupMenu popupMenu = new JPopupMenu()
            // Add menu items to the popup menu
            popupMenu.add(new JMenuItem(text:'    Copy',font:new Font('Dialog', Font.PLAIN, 18),actionPerformed:{
                copyToClipboard(inputTextArea.text)
            }))
            popupMenu.add(new JMenuItem(text:'    Cut',font:new Font('Dialog', Font.PLAIN, 18),actionPerformed:{
                copyToClipboard(inputTextArea.text)
                inputTextArea.text = ""
            }))
            popupMenu.add(new JMenuItem(text:'    Paste',font:new Font('Dialog', Font.PLAIN, 18),actionPerformed:{
                inputTextArea.text = getClipboard()
            }))
            popupMenu.add(new JMenuItem(text:'    Clear',font:new Font('Dialog', Font.PLAIN, 18),actionPerformed:{
                inputTextArea.text = ""
            }))
            popupMenu.add(new JSeparator())
            addBindingPopupMenu = popupMenu.add(new JMenuItem(text:'    Add binding variable',enabled:false, font:new Font('Dialog', Font.PLAIN, 18),actionPerformed:{
                newVariablePopUpFrame.setLocationRelativeTo(null)
                newVariablePopUpFrame.setVisible(true)
            }))
            editBindingPopupMenu = popupMenu.add(new JMenuItem(text:'    Edit binding variable',enabled:false, font:new Font('Dialog', Font.PLAIN, 18),actionPerformed:{
                updateVariablePopUpFrame.setLocationRelativeTo(null)
                updateVariablePopUpFrame.setVisible(true)
            }))
            popupMenu.add(new JSeparator())
            popupMenu.add(new JMenuItem(text:'    Validate JSON',font:new Font('Dialog', Font.PLAIN, 18),actionPerformed:{
                if(inputTextArea.text.trim() != ""){
                    try{
                        if(isValidJson(inputTextArea.text,true)){
                            def textField = new JTextField(text:"Valid JSON!",font:new Font('Dialog', Font.PLAIN, 18))
                            textField.setEditable(false)
                            JOptionPane.showMessageDialog(myFrame, textField, "OK", JOptionPane.INFORMATION_MESSAGE)
                        } else{
                            def textField = new JTextField(text:"Invalid JSON!",font:new Font('Dialog', Font.PLAIN, 18))
                            textField.setEditable(false)
                            JOptionPane.showMessageDialog(myFrame, textField, "OK", JOptionPane.ERROR_MESSAGE)
                        }
                    } catch(JsonProcessingException e){
                        def textField = new JTextField(text:"Invalid JSON: ${e.getMessage()}",font:new Font('Dialog', Font.PLAIN, 18))
                        textField.setEditable(false)
                        JOptionPane.showMessageDialog(myFrame, textField, "OK", JOptionPane.ERROR_MESSAGE)
                    }
                }
            }))
            popupMenu.add(new JMenuItem(text:'    Beautify JSON',font:new Font('Dialog', Font.PLAIN, 18),actionPerformed:{
                prettyString = JsonOutput.prettyPrint(inputTextArea.text)
                if(prettyString==""){
                    prettyString = inputTextArea.text
                }
                inputTextArea.text = prettyString
            }))
            popupMenu.add(new JMenuItem(text:'    Minify JSON',font:new Font('Dialog', Font.PLAIN, 18),actionPerformed:{
                inputTextArea.text = minifyJSON(inputTextArea.text)
            }))

            // Attach the popup menu to the text area
            inputTextArea.addMouseListener(new MouseAdapter() {
                void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        inputTextAreaPopupPreProcess()
                        popupMenu.show(e.getComponent(), e.getX(), e.getY())
                    }
                }
                void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        inputTextAreaPopupPreProcess()
                        popupMenu.show(e.getComponent(), e.getX(), e.getY())
                    }
                }
            })
        }
        panel(background: new Color(40, 40, 40),preferredSize: [widthTextArea*0.25 as int, heightTextArea as int],layout: new GridBagLayout()) {
            def constraints = new GridBagConstraints()
            constraints.gridx = 0
            constraints.gridy = 0
            constraints.fill = GridBagConstraints.HORIZONTAL
            constraints.insets = new Insets(0, 0, 20, 0)  // 10 pixels gap at the bottom
            def font = new Font('Sans Serif', Font.BOLD, 14*size_factor);
            def preferredSize = [widthTextArea*0.25*0.9 as int, heightTextArea*0.05 as int];
			resolveButton = button(text: 'Resolve >>', constraints:constraints, font: font,preferredSize: preferredSize, background: Color.BLACK, foreground: Color.WHITE,border :new LineBorder(Color.WHITE, 2), actionPerformed: {
				try{
					def input = inputTextArea.text
					if(input!=null && input.trim().startsWith("desc ")){
						def descBinding = input.trim().substring(5).trim()
						def description = buildDefaultBindingVariables().get(descBinding)
						if(description==null){
                            outputTextArea.foreground = Color.RED
							outputTextArea.text = "Unable to describe: [$descBinding]\n"
						} else{
							Map descriptionMap = new HashMap()
							descriptionMap.put(descBinding,description)
							def descriptionJson = new JsonBuilder( descriptionMap ).toPrettyString()
                            outputTextArea.foreground = Color.WHITE
							outputTextArea.text = descriptionJson
						}
					} else{
						def output = resolveGroovyScript(input)
                        if(isValidJson(output)){
                            outputTextArea.foreground = Color.GREEN
                            try{
                                finalResolvedResultJSON = JsonOutput.prettyPrint(output)
                                if(finalResolvedResultJSON==""){
                                    finalResolvedResultJSON = finalResolvedResult
                                }
                                output = finalResolvedResultJSON
                            } catch(Exception e){
                                //println("Invalid JSON")
                            }
                        } else{
                            outputTextArea.foreground = Color.WHITE
                        }
						outputTextArea.text = output
					}
                    if(beta_feature){
                        populateInputTextArea(inputTextArea.text)
                    }
				} catch(groovy.lang.MissingPropertyException e){
                    outputTextArea.foreground = Color.RED
					outputTextArea.text = e.getMessage()+"\n"
                    if(beta_feature){                    
                        missingVariableName = e.getMessage().replaceAll("Binding variables missing..\n\n\nError = No such property: ","")
                        def index = missingVariableName.indexOf(" for class:")
                        if (index != -1) {
                            missingVariableName = missingVariableName.substring(0, index)
                        } else {
                            missingVariableName=null
                        }
                        populateInputTextArea(inputTextArea.text, missingVariableName)
                    }
				} catch(NullPointerException e) {
                    outputTextArea.foreground = Color.RED
					outputTextArea.text = e.getMessage()+"\n"
                    if(beta_feature){  
                        def index = null;
                        if(e.getMessage().startsWith("NullPointerException..\n\n\nError = Cannot get property '")){
                            missingVariableName = e.getMessage().replaceAll("NullPointerException..\n\n\nError = Cannot get property '","")
                            index = missingVariableName.indexOf("' on null object")
                        } else if(e.getMessage().startsWith("NullPointerException..\n\n\nError = Cannot invoke method ")){
                            missingVariableName = e.getMessage().replaceAll("NullPointerException..\n\n\nError = Cannot invoke method ","")
                            index = missingVariableName.indexOf("() on null object")
                        }          
                        
                        if (index!=null && index != -1) {
                            missingVariableName = missingVariableName.substring(0, index)
                        } else {
                            missingVariableName=null
                        }
                        populateInputTextArea(inputTextArea.text, missingVariableName)
                    }
				} catch(Exception e){
                    outputTextArea.foreground = Color.RED
					outputTextArea.text = e.getMessage()+"\n"
				}
	
			})
            constraints.gridy = 1
            bindingButton = button(text: 'Bindings', constraints:constraints, font: font,preferredSize: preferredSize, background: Color.BLACK, foreground: Color.WHITE,border :new LineBorder(Color.WHITE, 2), actionPerformed: {
                try{
                    Map bindingMapAll = buildDefaultBindingVariables()
                    def bindingMapAllSorted = bindingMapAll.sort { a, b -> a.key <=> b.key }

                    def descriptionJson = new JsonBuilder( bindingMapAllSorted ).toPrettyString()
                    createAndShowGUI(descriptionJson)
				} catch(Exception e){
                    outputTextArea.foreground = Color.RED
					outputTextArea.text = e.getMessage();
				}
            })
            constraints.gridy = 2
            /*
			clearButton = button(text: 'Clear',constraints:constraints,font: font,preferredSize: preferredSize, background: Color.BLACK, foreground: Color.WHITE,border :new LineBorder(Color.WHITE, 2), actionPerformed: {
				outputArea.text = "";
				input.text = "";
	
			})
            
            constraints.gridy = 3
            addNewVariable = button(text: 'Add Variable',constraints:constraints,font: font ,preferredSize: preferredSize, background: Color.BLACK, foreground: Color.WHITE,border :new LineBorder(Color.WHITE, 2), actionPerformed: {
                newVariablePopUpFrame.setLocationRelativeTo(null)
                newVariablePopUpFrame.setVisible(true)
			})
            constraints.gridy = 4
            addNewVariable = button(text: 'Edit Variable',constraints:constraints,font: font,preferredSize: preferredSize, background: Color.BLACK, foreground: Color.WHITE,border :new LineBorder(Color.WHITE, 2), actionPerformed: {
                updateVariablePopUpFrame.setLocationRelativeTo(null)
                updateVariablePopUpFrame.setVisible(true)
			})    
            */        
            if(beta_feature){
                constraints.gridy = 5
                addNewVariable = button(text: 'Validate JSON',constraints:constraints,font: font ,preferredSize: preferredSize, background: Color.BLACK, foreground: Color.WHITE,border :new LineBorder(Color.WHITE, 2), actionPerformed: {
                    if (Desktop.isDesktopSupported()) {
                        def desktop = Desktop.getDesktop()
                        if (desktop.isSupported(Desktop.Action.BROWSE)) {
                            def originalString = outputTextArea.text
                            def base64String = Base64.getEncoder().encodeToString(originalString.bytes)
                            def urlEncodedBase64 = URLEncoder.encode(base64String, "UTF-8")
                            desktop.browse(new URI("https://vivek9237.github.io/json-validator?data=${urlEncodedBase64}"))
                        }
                    }
			    })
            }
            if(beta_feature){
                constraints.gridy = 6
                randomSamples = button(text: 'Surprise me!',constraints:constraints,font: font,preferredSize: preferredSize, background: Color.BLACK, foreground: Color.WHITE,border :new LineBorder(Color.WHITE, 2), actionPerformed: {
                    String fileString = readFileString("scriptConfigs/randomSamples.json")
                    def json = new groovy.json.JsonSlurper().parseText(fileString) as List
                    Random random = new Random()
                    int randomIndex = random.nextInt(json.size())
                    inputTextArea.text =json.get(randomIndex)
                    resolveButton.doClick();
			    })
            }
        }
        outputScrollPane = scrollPane(preferredSize: [widthTextArea as int, heightTextArea as int]) {
            //outputTextArea = textArea(id: 'outputArea', columns: 60, rows: 30,lineWrap:false, wrapStyleWord:false, editable: false, background: Color.BLACK, foreground: Color.WHITE, font: new Font('Courier New', Font.BOLD, 14*size_factor))
            outputTextArea = widget(new RSyntaxTextArea(syntaxEditingStyle: RSyntaxTextArea.SYNTAX_STYLE_GROOVY,codeFoldingEnabled:true,antiAliasingEnabled:true,font: new Font('Courier New', Font.PLAIN, 14*size_factor)))
            Theme darkTheme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/monokai.xml"))
            darkTheme.apply(outputTextArea)
            outputTextArea.setFont(new Font('Courier New', Font.PLAIN, 14*size_factor))
            outputTextArea.setBackground(Color.BLACK)
                        // Create a popup menu
            JPopupMenu popupMenu = new JPopupMenu()
            // Add menu items to the popup menu
            popupMenu.add(new JMenuItem(text:'    Copy',font:new Font('Dialog', Font.PLAIN, 18),actionPerformed:{
                copyToClipboard(outputTextArea.text)
            }))
            popupMenu.add(new JMenuItem(text:'    Cut',font:new Font('Dialog', Font.PLAIN, 18),actionPerformed:{
                copyToClipboard(outputTextArea.text)
                outputTextArea.text = ""
            }))
            popupMenu.add(new JMenuItem(text:'    Paste',font:new Font('Dialog', Font.PLAIN, 18),enabled:false,actionPerformed:{
                outputTextArea.text = getClipboard()
            }))
            popupMenu.add(new JMenuItem(text:'    Clear',font:new Font('Dialog', Font.PLAIN, 18),actionPerformed:{
                outputTextArea.text = ""
            }))
            popupMenu.add(new JSeparator())
            popupMenu.add(new JMenuItem(text:'    Validate JSON',font:new Font('Dialog', Font.PLAIN, 18),actionPerformed:{
                if(outputTextArea.text.trim() != ""){
                    try{
                        if(isValidJson(outputTextArea.text,true)){
                            def textField = new JTextField(text:"Valid JSON!",font:new Font('Dialog', Font.PLAIN, 18))
                            textField.setEditable(false)
                            JOptionPane.showMessageDialog(myFrame, textField, "OK", JOptionPane.INFORMATION_MESSAGE)
                        } else{
                            def textField = new JTextField(text:"Invalid JSON!",font:new Font('Dialog', Font.PLAIN, 18))
                            textField.setEditable(false)
                            JOptionPane.showMessageDialog(myFrame, textField, "OK", JOptionPane.ERROR_MESSAGE)
                        }
                    } catch(JsonProcessingException e){
                        def textField = new JTextField(text:"Invalid JSON: ${e.getMessage()}",font:new Font('Dialog', Font.PLAIN, 18))
                        textField.setEditable(false)
                        JOptionPane.showMessageDialog(myFrame, textField, "OK", JOptionPane.ERROR_MESSAGE)
                    }
                }
            }))
            popupMenu.add(new JMenuItem(text:'    Beautify JSON',font:new Font('Dialog', Font.PLAIN, 18),actionPerformed:{
                prettyString = JsonOutput.prettyPrint(outputTextArea.text)
                if(prettyString==""){
                    prettyString = outputTextArea.text
                }
                outputTextArea.text = prettyString
            }))
            popupMenu.add(new JMenuItem(text:'    Minify JSON',font:new Font('Dialog', Font.PLAIN, 18),actionPerformed:{
                outputTextArea.text = minifyJSON(outputTextArea.text)
            }))

            // Attach the popup menu to the text area
            outputTextArea.addMouseListener(new MouseAdapter() {
                void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        popupMenu.show(e.getComponent(), e.getX(), e.getY())
                    }
                }
                void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        popupMenu.show(e.getComponent(), e.getX(), e.getY())
                    }
                }
            })
        }
    }
}
//to show the binding variables by default
//bindingButton.doClick();
//to show the sample input by default
placeholder = """{\n   "foo": "\${user.firstname}",\n   "bar": "\${user.customproperty1}"\n}"""
inputTextArea.text = placeholder
myFrame.setLocationRelativeTo(null)
myFrame.setExtendedState(Frame.MAXIMIZED_BOTH)
myFrame.setVisible(true)
splash.dispose();

def populateInputTextArea(String value, String missingVariable=null){
    def defaultAttr = new SimpleAttributeSet()
    def formatTextAttr = new SimpleAttributeSet()
    StyleConstants.setForeground(formatTextAttr, Color.BLACK)  // Set text color to blue
    StyleConstants.setBackground(formatTextAttr, Color.RED)  // Set text color to blue
    def doc = inputTextArea.styledDocument
    doc.remove(0, doc.length)
    if(missingVariable==null){
        doc.insertString(doc.length, value, defaultAttr)
    } else{
        def tempList = value.split(missingVariable)
        for (int i = 0; i < tempList.size(); i++) {
            if (i == tempList.size() - 1) {
                doc.insertString(doc.length, tempList[i], defaultAttr)
            } else {
                doc.insertString(doc.length, tempList[i], defaultAttr)
                doc.insertString(doc.length, missingVariable, formatTextAttr)
            }
        }
    }
}

def isValidJson(String jsonString, Boolean throwError = false) {
    /*
    def slurper = new groovy.json.JsonSlurper()
    try {
        def result = slurper.parseText(jsonString)
        return (result instanceof Map || result instanceof List)
    } catch (Exception e) {
        return false
    }*/
    ObjectMapper objectMapper = new ObjectMapper();
    try {
        // Attempt to parse the JSON string
        //println jsonString
        Object parsedObject = objectMapper.readValue(jsonString, Object.class);
        //println parsedObject
        return true;
    } catch (JsonProcessingException e) {
        if(throwError){
            throw new JsonProcessingException(e.getMessage())
        }
        return false;
    }
}

def updateExistingBindingVariable(String _bindingVariableName, String _bindingVariableValue) throws Exception{
    //println(defaultBindingMapGlobal)
    if(_bindingVariableName!=""){
        def properties = _bindingVariableName.split("\\.") // Splitting the input string to get property names
        currentObject = defaultBindingMapGlobal
        properties.eachWithIndex { prop, index ->
		    if(index < properties.size() - 1) {
			    currentObject = currentObject."$prop" // Navigating to the next object
			} else {
			    currentObject."$prop" = _bindingVariableValue // Setting the value to the final property
			}   
		}
    }
    bindingVariableMap.put(_bindingVariableName,_bindingVariableValue)
}
def addOrUpdateBindingVariable(_bindingVariableName,_bindingVariableValue){
    Map tempMap = new HashMap();
    tempMap = readBindingJsonFile(bindingJson)
    //
    if(_bindingVariableName!=""){
        def properties = _bindingVariableName.split("\\.") // Splitting the input string to get property names
        Map currentObject = tempMap
        properties.eachWithIndex { prop, index ->
            //println(currentObject)
			if(index < properties.size() - 1) {
                if(currentObject==null){
                    currentObject = new HashMap()
                    println("currentObject is null")
                } else{
                    println("currentObject is not null")
                }
                if(currentObject.get(prop.toString())==null){
                    currentObject.put(prop.toString(),new HashMap())
                    println("currentObject.get(prop.toString()) is null")
                } else{
                    println("currentObject.get(prop.toString()) is not null")
                }
			    currentObject = currentObject.get(prop.toString()) // Navigating to the next object
			} else {
			    currentObject.put(prop.toString(),_bindingVariableValue) // Setting the value to the final property
			}   
		}
    }
    def jsonString = new JsonBuilder(tempMap).toPrettyString()
    new File(bindingJson).text = jsonString
}
def readBindingJsonFile(fileName) throws Exception{
    Map bindingFromFile = new HashMap()
    try{
        String bindingJsonString = readFileString(fileName)
        //SimpleTemplateEngine engine = new SimpleTemplateEngine()
        //engine.escapeBackslash=true
        //bindingJsonString = engine.createTemplate(bindingJsonString).make(new HashMap());
        def json = new groovy.json.JsonSlurper().parseText(bindingJsonString) as HashMap
        bindingFromFile = json
    } catch (Exception e){
        throw new Exception("Error while loading binding variables from [${fileName}].\n\n"+e.getMessage())
    }
    return bindingFromFile
}
def resolveGroovyScript(String enteredString){
    String finalResolvedResult =""
    SimpleTemplateEngine engine = new SimpleTemplateEngine()
    try {
        Map bindingMap = buildDefaultBindingVariables()
        engine.escapeBackslash=true
        finalResolvedResult = engine.createTemplate(enteredString).make(bindingMap);
        return finalResolvedResult
    } catch(groovy.lang.MissingPropertyException e){
        finalResolvedResult = "Binding variables missing..\n\n\nError = "+e.getMessage()
        e.printStackTrace()
        throw new groovy.lang.MissingPropertyException("Binding variables missing..\n\n\nError = "+e.getMessage())
    } catch(NullPointerException e){
        finalResolvedResult = "NullPointerException..\n\n\nError = "+e.getMessage()
        e.printStackTrace()
        throw new NullPointerException("NullPointerException..\n\n\nError = "+e.getMessage())
    } catch(Exception e){
        finalResolvedResult = e.getMessage()
        e.printStackTrace()
        throw new Exception(e.getMessage())
    }
    return finalResolvedResult
}

def buildDefaultBindingVariables()  throws Exception{
    Map defaultBindingMap = new HashMap()
    GroovyShell shell = new GroovyShell()
	def loadBindingVariablesHook
    try{
        loadBindingVariablesHook = shell.parse(new File(core_binding_manipulation_file))
    } catch(Exception hookException){
        //println("${core_binding_manipulation_file} not found.")
    }
    if(loadBindingVariablesHook!=null){
        try{
            loadBindingVariablesHook.maniputaleBindingVariables(defaultBindingMap)
        } catch(Exception hookException){
            throw new Exception("Error while processing the method [maniputaleBindingVariables] in ${core_binding_manipulation_file} file.\n\n"+hookException.getMessage());
        }
    }
    try{
        defaultBindingMap.putAll(readBindingJsonFile(bindingJson))
    } catch(Exception bindingJsonException){
        //throw new Exception("Error while processing the ${bindingJson} file.\n\n"+bindingJsonException.getMessage());
    }
    defaultBindingMap = updateBindingVariableMap(defaultBindingMap)
    defaultBindingMapGlobal = defaultBindingMap;
    this.listOfBindingKeys = flattenMap( new groovy.json.JsonSlurper().parseText(new JsonBuilder( defaultBindingMap ).toPrettyString()) as HashMap)
    return defaultBindingMap;
}

def flattenMap(Map map, String parentKey = '') {
    def temp_listOfBindingKeys = []
    map.each { key, value ->
        def fullKey = parentKey ? "$parentKey.$key" : key
        if (value instanceof Map) {
            temp_listOfBindingKeys.addAll(flattenMap(value, fullKey)) // Recursive call for nested mapsa
        } else {
            temp_listOfBindingKeys << "$fullKey : $value"
        }
    }
    return temp_listOfBindingKeys
}

def flattenMapKeys(Map map, String parentKey = '') {
    Map<String,String> returnMap = new HashMap<String,String>();
    map.each { key, value ->
        def fullKey = parentKey ? parentKey+"."+key : key
        if (value instanceof Map) {
            returnMap.putAll(flattenMapKeys(value, fullKey)) // Recursive call for nested mapsa
        } else {
            returnMap.put(fullKey,"")
        }
    }
    return returnMap
}

def updateBindingVariableMap(Map defaultBindingMap) throws Exception{
    bindingVariableMap.each { key, value ->
        if(key!=""){
            def properties = key.split("\\.") // Splitting the input string to get property names
            currentObject = defaultBindingMap
            properties.eachWithIndex { prop, index ->
				if(index < properties.size() - 1) {
				    currentObject = currentObject."$prop" // Navigating to the next object
				} else {
				    currentObject."$prop" = value // Setting the value to the final property
				}   
			}
        }
    }  
    return defaultBindingMap;
}

def readBindingCoreJsonFile(Map defaultBindingMap){
     String fileName = "sav-core-binding.json"
     Map coreBindingFromFile = new HashMap()
     try{
        def file = new File(fileName)
        String bindingJson = file.readLines()
        def json = new groovy.json.JsonSlurper().parseText(bindingJson) as HashMap
        json.each{
            def bindingVar = it.key
            def bindingValue = it.value
            def tempValue = defaultBindingMap.get(bindingVar.toString())
            if(bindingValue instanceof Map && tempValue!=null){
                bindingValue.each{
                    bindingProperty = it.key
                    bindingPropertyValue = it.value
                    SimpleTemplateEngine engine = new SimpleTemplateEngine()
                    engine.escapeBackslash=true
                    def bindingPropertyValueObj = engine.createTemplate(bindingPropertyValue).make(new HashMap());
                    tempValue."${bindingProperty}" = bindingPropertyValueObj
                }
                coreBindingFromFile.put(bindingVar.toString(),tempValue)
            } else{
                coreBindingFromFile.put(bindingVar,bindingValue)
            }
        }
        //coreBindingFromFile = json
    } catch (Exception e){
        //e.printStackTrace()
    }
    return coreBindingFromFile
}
def readFileString(String filePath) {
    File file = new File(filePath)
    String fileContent = file.text
    return fileContent
}

private void createAndShowGUI(String jsonString) {
    JFrame frame = new JFrame("Binding Variables Explorer")
    def screenSize = Toolkit.getDefaultToolkit().getScreenSize()
    frame.setSize(screenSize.width*0.5 as int, screenSize.height*0.88 as int)
    frame.setLocationRelativeTo(null)
    frame.setAlwaysOnTop(true)
    // Parse JSON
    JsonSlurper jsonSlurper = new JsonSlurper()
    def jsonData = jsonSlurper.parseText(jsonString)

    // Create tree root
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Binding variables")
    createNodes(root, jsonData)

    // Create tree
    JTree tree = new JTree(root)
    //tree.setBackground(new Color(40, 40, 40))
    tree.setBackground(Color.BLACK)
    tree.setCellRenderer(new DefaultTreeCellRenderer() {
        @Override
        Component getTreeCellRendererComponent(JTree tree1, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            def label = super.getTreeCellRendererComponent(tree1, value, sel, expanded, leaf, row, hasFocus)
            label.setFont(new Font('Dialog', Font.PLAIN, 19)) // Set the desired font size here
            label.setOpaque(true) // Make the label opaque
            label.setForeground(Color.WHITE)
            //label.setBackground(new Color(40, 40, 40))
            label.setBackground(Color.BLACK)
            label.setBackgroundSelectionColor(Color.BLUE)
            label.setTextSelectionColor(Color.WHITE)
            return label
        }
    })
    JScrollPane treeView = new JScrollPane(tree)
    //popupMenu Code
    JPopupMenu popupMenu = new JPopupMenu()
    // Add menu items to the popup menu
    popupMenu.add(new JMenuItem(text:'    Add new variable',font:new Font('Dialog', Font.PLAIN, 18),actionPerformed:{
            newVariablePopUpFrame.setLocationRelativeTo(null)
            newVariablePopUpFrame.setVisible(true)
    }))
    popupMenu.add(new JMenuItem(text:'    Edit existing variable',font:new Font('Dialog', Font.PLAIN, 18),actionPerformed:{
            updateVariablePopUpFrame.setLocationRelativeTo(null)
            updateVariablePopUpFrame.setVisible(true)
    }))
    copyVariablePath = popupMenu.add(new JMenuItem(text:'    Copy variable path',font:new Font('Dialog', Font.PLAIN, 18), enabled:false,actionPerformed:{
    }))
    tree.addMouseListener(new MouseAdapter() {
        void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                def path = tree.getPathForLocation(e.x, e.y)
                showBindingExplorerOption(path)
                popupMenu.show(e.getComponent(), e.getX(), e.getY())
            }
        }
        void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                def path = tree.getPathForLocation(e.x, e.y)
                showBindingExplorerOption(path)
                popupMenu.show(e.getComponent(), e.getX(), e.getY())
            }
        }
    })
    frame.add(treeView)
    frame.setVisible(true)
}

def showBindingExplorerOption(def path){
	if (path != null) {
		def selectedNode = path.lastPathComponent
		if(path.toString().contains(":")){
			def textToCopy = "\${"+path.toString().replace("[Binding variables, ","").replace(", [","[").replace(", ","?.").split(":")?[0]+"}"
			def textField = new JTextField(text:textToCopy,font:new Font('Dialog', Font.PLAIN, 18))
			textField.setEditable(false)
			copyVariablePath.enabled = true
			copyVariablePath.text = "    Copy variable path '${textToCopy}'"
			copyToClipboard(textToCopy)
		} else{
			copyVariablePath.enabled = false
			copyVariablePath.text = "    Copy variable path"
		}
	} else{
		copyVariablePath.enabled = false
		copyVariablePath.text = "    Copy variable path"
	}
	
}
private void createNodes(DefaultMutableTreeNode node, def jsonData) {
	if (jsonData instanceof Map || jsonData instanceof LazyMap) {
		jsonData.each { key, value ->
			if (value instanceof Map || value instanceof List || value instanceof LazyMap) {
				DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(key)
				node.add(childNode)
				createNodes(childNode, value)
			} else {
				node.add(new DefaultMutableTreeNode("$key: $value"))
			}
		}
	} else if (jsonData instanceof List) {
		jsonData.eachWithIndex { value, index ->
			if (value instanceof Map || value instanceof List || value instanceof LazyMap) {
				DefaultMutableTreeNode childNode = new DefaultMutableTreeNode("[$index]")
				node.add(childNode)
				createNodes(childNode, value)
			} else {
				node.add(new DefaultMutableTreeNode("[$index]: $value"))
			}
		}
	}
}

def copyToClipboard(String text){
    // Get the system clipboard
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard()
    // Create a StringSelection object to hold the text
    StringSelection stringSelection = new StringSelection(text)
    // Set the contents of the clipboard to the StringSelection object
    clipboard.setContents(stringSelection, null)
}
def getClipboard(){
    // Get the system clipboard
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard()
    // Check if the clipboard contains text data
    if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
        try {
            // Get the text from the clipboard
            String text = clipboard.getData(DataFlavor.stringFlavor) as String
            return text
        } catch (Exception e) {
            //println("Error reading from clipboard: ${e.message}")
        }
    }
}

def minifyJSON(String jsonString){
    // Parse the JSON string
    def jsonSlurper = new JsonSlurper()
    def jsonObject = jsonSlurper.parseText(jsonString)
    // Convert back to a minified JSON string
    String minifiedJson = JsonOutput.toJson(jsonObject)
    return minifiedJson;
}
void inputTextAreaPopupPreProcess(){
	String selectedText = inputTextArea.selectedText
	if(selectedText != null && !selectedText.trim().equals("")) {
		selectedText = selectedText.trim().replace("{","").replace("}","").replace("\$","").replace("?","")
		Map bindingKeysMap = flattenMapKeys(new groovy.json.JsonSlurper().parseText(new JsonBuilder( buildDefaultBindingVariables() ).toPrettyString()) as HashMap)
        String presentString = bindingKeysMap.get(selectedText)
		if (presentString!=null) {
			editBindingPopupMenu.enabled = true
			addBindingPopupMenu.enabled = false
            updateBindingVariableName.selectedItem = selectedText
		} else {
			editBindingPopupMenu.enabled = false
			addBindingPopupMenu.enabled = true
            newVariableBindingVariableName.text = selectedText
		}
	} else{
		editBindingPopupMenu.enabled = true
		addBindingPopupMenu.enabled = true
        updateBindingVariableName.selectedItem = null
        newVariableBindingVariableName.text = null
	}
}