/*
 * Copyright (c) 2008-2013 Maksim Khadkevich and Fondazione Bruno Kessler.
 *
 * This file is part of MART.
 * MART is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2, as published
 * by the Free Software Foundation.
 *
 * MART is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with MART; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.mart.crs.analysis.filter.QMF;

import org.mart.crs.utils.helper.HelperFile;

import java.util.*;

/**
 * @version 1.0 27.04.2009 15:57:58
 * @author: Maksim Khadkevich
 */
public class QMFTree {

    public static final String LEFT_NODE = "0";
    public static final String RIGHT_NODE = "1";


    private HashMap<String, QMFTreeNode> treeMap;
    private Set<String> treeNodesConfig;
    private String[] finalNodes;

    float samplingRate;


    public QMFTree(String[] treeConfig, float[] samples, float samplingRate) {
        initialize(treeConfig, samples, samplingRate);
    }

    public QMFTree(String configFilePath, float[] samples, float samplingRate) {
        List<String> nodes = HelperFile.readTokensFromTextFile(configFilePath, Integer.MAX_VALUE);
        String[] nodesArray = new String[nodes.size()];
        nodes.toArray(nodesArray);
        initialize(nodesArray, samples, samplingRate);
    }

    public void initialize(String[] treeConfig, float[] samples, float samplingRate) {
        this.samplingRate = samplingRate;
        this.finalNodes = treeConfig;
        this.treeMap = new HashMap<String, QMFTreeNode>();
        treeNodesConfig = new TreeSet<String>();
        for (String id : treeConfig) {
            for (String parentId : getAllParentsIDs(id)) {
                if (!treeNodesConfig.contains(parentId)) {
                    treeNodesConfig.add(parentId);
                }
            }
            treeNodesConfig.add(id);
        }

        QMFTreeNode rootNode = QMFTreeNode.getRootNode();
        QMF.addChildren(this, rootNode, samples);
    }


    public void addNode(QMFTreeNode node) {
        treeMap.put(node.getId(), node);
    }


    public void removeNode(QMFTreeNode node) {
        //First remove node
        treeMap.remove(node.getId());
        treeNodesConfig.remove(node.getId());

        //Now remove all it's children
        boolean changed = true;
        while (changed) {
            Set<String> keySet = treeMap.keySet();
            changed = false;
            for (String key : keySet) {
                if (key.startsWith(node.getId())) {
                    treeMap.remove(key);
                    treeNodesConfig.remove(key);
                    changed = true;
                    break;
                }
            }

        }

    }

    public void leaveOnlyParents(QMFTreeNode node) {
        //Now remove all it's children
        boolean changed = true;
        while (changed) {
            Set<String> keySet = treeMap.keySet();
            changed = false;
            for (String key : keySet) {
                if (!node.getId().startsWith(key)) {
                    treeMap.remove(key);
                    treeNodesConfig.remove(key);
                    changed = true;
                    break;
                }
            }

        }
    }


    public QMFTreeNode getNode(String id) {
        return treeMap.get(id);
    }

    public boolean hasLeftChild(QMFTreeNode node) {
        if (treeNodesConfig.contains(node.getId() + LEFT_NODE)) {
            return true;
        }
        return false;
    }

    public boolean hasRightChild(QMFTreeNode node) {
        if (treeNodesConfig.contains(node.getId() + RIGHT_NODE)) {
            return true;
        }
        return false;
    }

    public QMFTreeNode getLeftChild(QMFTreeNode node) {
        String id = node.getId() + LEFT_NODE;
        if (treeNodesConfig.contains(id)) {
            return treeMap.get(id);
        } else {
            return null;
        }
    }


    public QMFTreeNode getRightChild(QMFTreeNode node) {
        String id = node.getId() + RIGHT_NODE;
        if (treeNodesConfig.contains(id)) {
            return treeMap.get(id);
        } else {
            return null;
        }
    }

    public boolean hasAChild(QMFTreeNode node) {
        return hasLeftChild(node) || hasRightChild(node);
    }


    public boolean isFinalNode(String id) {
        boolean result = true;
        Set<String> keys = treeNodesConfig;
        for (String key : keys) {
            if (key.startsWith(id) && !key.equals(id)) {
                result = false;
                break;
            }
        }
        return result;
    }


    public boolean isFinalRightNode(String id) {
        return isFinalNode(id + RIGHT_NODE);
    }

    public boolean isFinalLeftNode(String id) {
        return isFinalNode(id + LEFT_NODE);
    }


    public List<String> getAllParentsIDs(String id) {
        List<String> outList = new ArrayList<String>();
        for (int i = 0; i < id.length(); i++) {
            outList.add(id.substring(0, i));
        }
        return outList;
    }


    public String[] getFinalNodesIds() {
        return this.finalNodes;
    }

    public List<QMFTreeNode> getFinalTreeNodes() {
        List<QMFTreeNode> out = new ArrayList<QMFTreeNode>();
        for (String finalNode : finalNodes) {
            out.add(getNode(finalNode));
        }
        Collections.sort(out);
        return out;
    }


}