/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 */
package com.liveperson.infra.akka.actorx.ui;

import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.util.Animator;
import org.apache.commons.collections15.Transformer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Partially based on ShowLayouts2 by Danyel Fisher & Joshua O'Madadhain
 */
@SuppressWarnings("serial")
public class DisplayGraph extends JApplet {

    static Graph<String, String> graph;
    static Map<String, Map<String, Set<String>>> castConnectionList;

    static void setGraph(Graph<String, String> graph) {
        DisplayGraph.graph = graph;
    }

    static void setCastConnectionList(Map<String, Map<String, Set<String>>> castConnectionList) {
        DisplayGraph.castConnectionList = castConnectionList;
    }

    private static final class LayoutChooser implements ActionListener
    {
        private final JComboBox jcb;
        private final VisualizationViewer<String,String> vv;

        private LayoutChooser(JComboBox jcb, VisualizationViewer<String,String> vv)
        {
            super();
            this.jcb = jcb;
            this.vv = vv;
        }

        public void actionPerformed(ActionEvent arg0)
        {
            Object[] constructorArgs =
                { graph};

            Class<? extends Layout<String,String>> layoutC =
                (Class<? extends Layout<String,String>>) jcb.getSelectedItem();
            try
            {
                Constructor<? extends Layout<String,String>> constructor = layoutC
                        .getConstructor(new Class[] {Graph.class});
                Object o = constructor.newInstance(constructorArgs);
                Layout<String,String> l = (Layout<String,String>) o;
                l.setInitializer(vv.getGraphLayout());
                l.setSize(vv.getSize());
                
				LayoutTransition<String,String> lt =
					new LayoutTransition<String,String>(vv, vv.getGraphLayout(), l);
				Animator animator = new Animator(lt);
				animator.start();
				vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
				vv.repaint();
                
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static JPanel getGraphPanel() {

        final VisualizationViewer<String,String> vv =
            new VisualizationViewer<String,String>(new FRLayout(graph));

        vv.setBackground(Color.white);

        vv.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<String>(vv.getPickedVertexState(), Color.blue, Color.yellow));
        vv.getRenderContext().setVertexLabelTransformer(new Transformer<String, String>() {
            @Override
            public String transform(String s) {
                return getClassName(s);
            }
        });
        vv.setVertexToolTipTransformer(new Transformer<String, String>() {
            @Override
            public String transform(String s) {
                return getClassName(s);
            }
        });

        vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<String>(vv.getPickedEdgeState(), Color.black, Color.green));
        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.QuadCurve<String, String>());
        vv.setEdgeToolTipTransformer(new Transformer<String, String>() {
            @Override
            public String transform(String edge) {
                StringBuffer buffer = new StringBuffer();
                buffer.append("<html>");
                int index = edge.indexOf(Main.DELIMITER);
                String fromActor = edge.substring(0, index);
                String toActor = edge.substring(index + Main.DELIMITER.length());
                Map<String, Set<String>> connections = castConnectionList.get(fromActor);
                Set<String> messages = connections.get(toActor);
                for (String msg : messages) {
                    buffer.append("<p>").append(getClassName(msg));
                }
                buffer.append("</html>");
                return buffer.toString();
            }
        });
        ToolTipManager.sharedInstance().setDismissDelay(60000);

        final DefaultModalGraphMouse<String,String> graphMouse = new DefaultModalGraphMouse<String,String>();
        graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
        vv.setGraphMouse(graphMouse);

        
        final ScalingControl scaler = new CrossoverScalingControl();

        JButton plus = new JButton("+");
        plus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1.1f, vv.getCenter());
            }
        });
        JButton minus = new JButton("-");
        minus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1/1.1f, vv.getCenter());
            }
        });
        JButton reset = new JButton("reset");
        reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Layout<String,String> layout = vv.getGraphLayout();
				layout.initialize();
				Relaxer relaxer = vv.getModel().getRelaxer();
				if(relaxer != null) {
//				if(layout instanceof IterativeContext) {
					relaxer.stop();
					relaxer.prerelax();
					relaxer.relax();
				}
			}});
        
        JPanel jp = new JPanel();
        jp.setBackground(Color.WHITE);
        jp.setLayout(new BorderLayout());
        jp.add(vv, BorderLayout.CENTER);
        Class[] combos = getCombos();
        final JComboBox jcb = new JComboBox(combos);
        // use a renderer to shorten the layout name presentation
        jcb.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String valueString = value.toString();
                valueString = valueString.substring(valueString.lastIndexOf('.')+1);
                return super.getListCellRendererComponent(list, valueString, index, isSelected,
                        cellHasFocus);
            }
        });
        jcb.addActionListener(new LayoutChooser(jcb, vv));
        jcb.setSelectedItem(FRLayout.class);

        JPanel control_panel = new JPanel(new GridLayout(2,1));
        JPanel topControls = new JPanel();
        JPanel bottomControls = new JPanel();
        control_panel.add(topControls);
        control_panel.add(bottomControls);
        jp.add(control_panel, BorderLayout.NORTH);
        

        topControls.add(jcb);
        bottomControls.add(plus);
        bottomControls.add(minus);
        bottomControls.add(reset);
        return jp;
    }

    public void start()
    {
        this.getContentPane().add(getGraphPanel());
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    private static Class<? extends Layout>[] getCombos()
    {
        List<Class<? extends Layout>> layouts = new ArrayList<Class<? extends Layout>>();
        layouts.add(KKLayout.class);
        layouts.add(FRLayout.class);
        layouts.add(CircleLayout.class);
        layouts.add(SpringLayout.class);
        layouts.add(SpringLayout2.class);
        layouts.add(ISOMLayout.class);
        return layouts.toArray(new Class[0]);
    }

    private static String getClassName(String className) {
        String prettyName = className;
        if (className != null) {
            int lastDot = prettyName.lastIndexOf(".");
            if (lastDot > 0) {
                prettyName = prettyName.substring(lastDot + 1);
            }
        }
        return prettyName;
    }}
