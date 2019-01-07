package org.protege.editor.owl.rdf;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.editor.core.ui.error.ErrorLogPanel;
import org.protege.editor.owl.rdf.repository.BasicSparqlReasonerFactory;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.protege.editor.owl.ui.table.BasicOWLTable;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.protege.owl.rdf.Utilities;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class SparqlQueryView extends AbstractOWLViewComponent {
	private static final long serialVersionUID = -1370725700740073290L;
	
	protected static final String LAST_EXPORT_FOLDER = "";
	
	private SparqlReasoner reasoner;
	private JList bookmarkList;
	private JTextPane queryPane;
	private JButton executeQuery;
	private JButton bookmarkBtn;
	private JButton exportBtn;
	private SwingResultModel resultModel;
	
	private Map<String, String> queryMap;
	
	@Override
	protected void initialiseOWLView() throws Exception {
		initializeReasoner();
		initializeQueryMap();
		setLayout(new BorderLayout());
		add(createCenterComponent(), BorderLayout.CENTER);
		add(createBottomComponent(), BorderLayout.SOUTH);
	}
	
	private void initializeReasoner() {
		try {
			List<SparqlInferenceFactory> plugins = Collections.singletonList((SparqlInferenceFactory) new BasicSparqlReasonerFactory());
			reasoner = plugins.iterator().next().createReasoner(getOWLModelManager().getOWLOntologyManager());
			reasoner.precalculate();
		}
		catch (SparqlReasonerException e) {
			ErrorLogPanel.showErrorDialog(e);
		}
	}
	
	private void initializeQueryMap() {
		
		queryMap = new HashMap<String, String>();
		
		String filename = "." + File.separator + "bookmark";
		File file = new File(filename);
		if (!file.isDirectory()) {
			file.mkdir();
	
		}
		String[] filenames = file.list();
		
		try {
			for(int i = 0; i < filenames.length; i++) {
				File localfile = new File("." + File.separator + "bookmark" + File.separator + filenames[i]);
				Scanner scanner = new Scanner(localfile);
				
				String value = scanner.useDelimiter("\\Z").next();
				
				String bookmark = null; 
				if(!filenames[i].contains(".")) {
					bookmark = filenames[i];
				}
				else {
					bookmark = filenames[i].substring(0, filenames[i].indexOf('.'));
				}
				
				queryMap.put(bookmark, value);
		
				scanner.close();
			}
		}
		catch(Exception ex) {
			ErrorLogPanel.showErrorDialog(ex);
			JOptionPane.showMessageDialog(getOWLWorkspace(), ex.getMessage() + "\nSee the logs for more information.");			
		}
		
	}
	private JComponent createCenterComponent() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0,1));
		DefaultListModel<String> model = new DefaultListModel<>();
		Set<String> bookmarkKeys = queryMap.keySet();
		//first add new query to list and newquery key pair to hasmap
		model.addElement("NewQuery");		
		
		for(String bookmark : bookmarkKeys) {
			model.addElement(bookmark);
		}
		
		queryMap.put("NewQuery", "");
		bookmarkList = new JList(model);
		queryPane = new JTextPane();
		
		bookmarkList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		bookmarkList.setSelectedIndex(0);
		
		//String firstBookmarkKey = bookmarkKeys.iterator().next();
		queryPane.setText(queryMap.get(bookmarkList.getSelectedValue().toString()));
		
		bookmarkList.addListSelectionListener(new ListSelectionListener() {
			@Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                  queryPane.setText(queryMap.get(bookmarkList.getSelectedValue().toString()));
                }
            }
		});
		
		
		//queryPane.setText(reasoner.getSampleQuery());
		JScrollPane bookmarkSP = new JScrollPane(bookmarkList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JScrollPane querySP = new JScrollPane(queryPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, bookmarkSP, querySP);
		//splitPane.setDividerLocation(0.35);
		splitPane.setResizeWeight(0.5);
		panel.add(splitPane);
		resultModel = new SwingResultModel();
		BasicOWLTable results = new BasicOWLTable(resultModel) {
			private static final long serialVersionUID = 9143285439978520141L;

			@Override
			protected boolean isHeaderVisible() {
				return true;
			}
		};
		OWLCellRenderer renderer = new OWLCellRenderer(getOWLEditorKit());
		renderer.setWrap(false);
		results.setDefaultRenderer(Object.class, renderer);
		JScrollPane scrollableResults = new JScrollPane(results);
		panel.add(scrollableResults);
		return panel;
	}
	
	private JComponent createBottomComponent() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 10));
		executeQuery = new JButton("Execute");
		//executeQuery.
		
		
		executeQuery.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					String query = queryPane.getText();
					long beg = System.currentTimeMillis();
					
					//panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					
					SparqlResultSet result = reasoner.executeQuery(query);
					System.out.println("The query took " + (System.currentTimeMillis() - beg));
					if (result.getRowCount() == 0) {
						List<Object> row = new ArrayList<Object>();
						
						row.add("NO RESULTS FOUND!!!");
						
						for (int i = 1; i < result.getColumnCount(); i++) {
							row.add("");
						}
						result.addRow(row);
						
					}
					resultModel.setResults(result);
					
					//panel.setCursor(Cursor.getDefaultCursor());
				}
				catch (SparqlReasonerException ex) {
					ErrorLogPanel.showErrorDialog(ex);
					JOptionPane.showMessageDialog(getOWLWorkspace(), ex.getMessage() + "\nSee the logs for more information.");
				}
			}
		});
		
		panel.add(executeQuery);
		
		bookmarkBtn = new JButton("Bookmark");
		bookmarkBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					String query = queryPane.getText();
					String bookmark = JOptionPane.showInputDialog("Enter bookmark: ");
					if (bookmark != null && !bookmark.isEmpty()) {
						if(Utilities.isInteger(bookmark)) {
							if (Integer.parseInt(bookmark) != JOptionPane.CANCEL_OPTION) {
								saveBookmark(bookmark, query);
							}
						} else {
							saveBookmark(bookmark, query);
							//queryMap.put(bookmark, query);
							//System.out.println("bookmark is added. bookmark name = " + bookmark);
							//System.out.println("query = " + query);
						}
					}
				} catch (Exception ex) {
					ErrorLogPanel.showErrorDialog(ex);
					JOptionPane.showMessageDialog(getOWLWorkspace(), ex.getMessage() + "\nSee the logs for more information.");
				}
			}
		});
		
		panel.add(bookmarkBtn);
		
		exportBtn = new JButton("Export");
		exportBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Preferences prefs = PreferencesManager.getInstance().getApplicationPreferences(getClass());   
					JFileChooser fc = new JFileChooser(prefs.getString(LAST_EXPORT_FOLDER, new File(".").getAbsolutePath()));
					 // if(inputFolder != null){
					//	  fc.setCurrentDirectory(inputFolder);
					//  }
					  //todo - add file extension filter
					  int select = fc.showSaveDialog(null);

					if (select == JFileChooser.APPROVE_OPTION) {
						 File file = fc.getSelectedFile();
						 prefs.putString(LAST_EXPORT_FOLDER, file.getParent());
						  //String outfile = file.getAbsolutePath();
						 FileWriter writer = new FileWriter(file);
							//System.out.println(query);
						 for(int i = 0; i < resultModel.getRowCount(); i++) {
							 int colcount = resultModel.getColumnCount();
							 StringBuilder builder = new StringBuilder();
							 for(int j = 0; j < colcount; j++) {
								 if (resultModel.getValueAt(i, j) != null) {
									 builder.append(resultModel.getValueAt(i, j).toString());
								 }
								 if(j < (colcount -1)) {
									 builder.append('\t');
								 }
							 }
							 
							 
							  writer.write( builder.toString() + '\n');
						 }
							
						writer.flush();
						writer.close();
						  
					 }
					
				}
				catch (Exception ex) {
					ErrorLogPanel.showErrorDialog(ex);
					JOptionPane.showMessageDialog(getOWLWorkspace(), ex.getMessage() + "\nSee the logs for more information.");
				}
			}
		});
		
		
		panel.add(exportBtn);
		
		
		return panel;
	}

	private void saveBookmark(String bookmark, String query) throws IOException {
		
		String filename = "." + File.separator + "bookmark" + File.separator + bookmark + ".txt";
		File file = new File(filename);
		file.getParentFile().mkdirs();
		FileWriter writer = new FileWriter(file);
		System.out.println(query);
		writer.write(query);
		writer.flush();
	    writer.close();
	    
	    //update queryMap
	    if (queryMap.get(bookmark) != null) {
	    	// bookmakr already exists this is an update
	    } else {
	    	DefaultListModel<String> model = (DefaultListModel)bookmarkList.getModel();
	    	model.addElement(bookmark);
	    	bookmarkList.setSelectedIndex(model.getSize() - 1);
	    }
	    queryMap.put(bookmark, query);
		
	}
	
	@Override
	protected void disposeOWLView() {
		if (reasoner != null) {
			reasoner.dispose();
			reasoner = null;
		}
	}
	
	

}
