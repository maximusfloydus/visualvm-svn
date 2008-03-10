/*
 * ProbeStatsPanel.java
 *
 * Created on February 22, 2008, 6:39 PM
 */
package net.java.visualvm.btrace.views;

import com.sun.tools.visualvm.core.model.jvm.MonitoredData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.SwingUtilities;
import net.java.visualvm.btrace.config.ProbeConfig.ProbeConnection;

/**
 *
 * @author  Jaroslav Bachorik
 */
public class ProbeStatsPanel extends javax.swing.JPanel {

    final private List<ProbeConnection> statsProviders = new ArrayList<ProbeConnection>();

    /** Creates new form ProbeStatsPanel */
    public ProbeStatsPanel(Collection<ProbeConnection> probeConnections) {
        initComponents();
        statsProviders.clear();
        statsProviders.addAll(probeConnections);
        Collections.sort(statsProviders, new Comparator<ProbeConnection>() {

            public int compare(ProbeConnection o1, ProbeConnection o2) {
                return o1.name.compareTo(o2.name);
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        statsOutputPanel = new net.java.visualvm.btrace.utils.HTMLTextArea();

        jScrollPane1.setViewportView(statsOutputPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private net.java.visualvm.btrace.utils.HTMLTextArea statsOutputPanel;
    // End of variables declaration//GEN-END:variables
    
    public void refresh(MonitoredData data) {
        final String infoHtml = buildInfo(data);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                statsOutputPanel.setText(infoHtml);
            }
        });
    }

    private String buildInfo(MonitoredData data) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<table>");
        for (ProbeConnection pc : statsProviders) {
            sb.append("<tr>");
            sb.append("<td>").append(pc.name).append("</td>");
//            sb.append("<td>").append(data.getByName(pc.jvmStatVar)).append("</td>");
            sb.append("</tr>");
        }

        sb.append("</table>");
        sb.append("</html>");

        return sb.toString();
    }
}
