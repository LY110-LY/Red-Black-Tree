package com.dp.dnsc.util;

import com.dp.plat.type.IpAddrNg;

/**
 * ①创建RBTree，定义颜色
 * ②创建RBNode
 * ③辅助方法定义：parentOf(node)，isRed(node)，setRed(node)，setBlack(node)，inOrderPrint(RBNode tree)
 * ④左旋方法定义：leftRotate(node)
 * ⑤右旋方法定义：rightRotate(node)
 * ⑥公开插入接口方法定义：insert(String ipStrat, String ipEnd);
 * ⑦内部插入接口方法定义：insert(RBNode node);
 * ⑧修正插入导致红黑树失衡的方法定义：insertFIxUp(RBNode node);
 * ⑨测试红黑树正确性
 *
 *
 * 红黑树建树方法，用于对海量数据的IP做重复比较
 * @date 2024/2/21
 * @author l05455 梁毅
 */
public class RBTreeNew {

    //定义颜色常量
    private static final boolean RED = true;
    private static final boolean BLACK = false;

    //插入失败标识
    public static boolean insertFlag = true;

    //红黑树的树根
    private RBNode root;

    public RBNode getRoot() {
        return root;
    }

    /**
     * 公开的插入接口
     *
     * @param ipStart   起始IP
     * @param ipEnd     结束IP
     */
    public String insert(String ipStart, String ipEnd) {
        RBNode node = new RBNode();
        String res; //返回插入成功或失败的结果，成功为""，失败为具体IP段
        node.setIpStart(ipStart);
        node.setIpEnd(ipEnd);
        node.setColor(RED);
        res = insert(node);
//        System.out.println(insertFlag);
        return res;
    }

    /**
     * 内部插入接口定义
     */
    private String insert(RBNode node) {
        //1.找到插入的位置
        RBNode parent = null;
        RBNode x = this.root; //x即为每次对红黑树向下搜索的节点，直到找到节点为空时，则可对新节点进行插入动作
        while (x != null) {
            parent = x;

            //分别对插入的起始IP和结束IP 与 有可能作为其父节点的节点 作比较
            //如果发现重复或IP段有交叉，则返回错误信息
            String ipStart = node.ipStart;
            String ipEnd = node.ipEnd;
            String parentIpStart = parent.ipStart;
            String parentIpEnd = parent.ipEnd;

            int ipStartcmpA = ipStart.compareTo(parentIpStart);
            int ipStartcmpB = ipEnd.compareTo(parentIpStart);
            int ipEndcmpA = ipStart.compareTo(parentIpEnd);
            int ipEndcmpB = ipEnd.compareTo(parentIpEnd);

            if (ipStartcmpA < 0 && ipStartcmpB < 0) {
                //全部<0为搜索通过，且当前预备插入节点的IP段小于当前的父节点，进而获取此父节点的左孩子
                x = x.left;
            } else if (ipEndcmpA > 0 && ipEndcmpB > 0) {
                //全部>0为搜索通过，且当前预备插入节点的IP段大于当前的父节点，进而获取此父节点的右孩子
                x = x.right;
            } else {
                //其余情况均为判定失败，返回错误信息--存在冲突的IP段
                insertFlag = false;
//                System.out.println("IP大小冲突错误！");
                String res = new IpAddrNg(parent.ipStart).formatTo() + "-" + new IpAddrNg(parent.ipEnd).formatTo();
                return res;
            }
        }

        //当x节点的左孩子或右孩子为null时，证明x节点可以作为将插入新节点的父节点，进而判断作为左孩子还是右孩子
        node.parent = parent;

        if (parent != null) {
            if (node.ipStart.compareTo(parent.ipStart) < 0) {
                parent.left = node;
            } else {
                parent.right = node;
            }
        } else {
            this.root = node;
        }

        //插入之后需要进行修复红黑树，让红黑树再次平衡
        insertFixUp(node);
        return "";
    }

    /**
     * 插入后修复红黑树平衡的方法（分情况讨论）
     * |---情况1：红黑树为空树
     *              处理：直接把插入结点作为根结点，根节点赋黑色。
     * |---情况2：插入节点的IP段有交集
     *              处理：返回插入失败信息--插入失败IP段。
     * |---情况3：插入节点的父节点为黑色
     *              处理：对插入节点赋红色，直接插入即可。
     * |---情况4：插入节点的父节点为红色
     * |---情况4.1：叔叔节点存在，并且为红色（父-叔 双红）
     *                处理：父亲和叔叔节点改为黑色，爷爷节点改为红色，将爷爷节点设为当前节点继续平衡处理。
     * |---情况4.2：叔叔节点不存在，或者为黑色，父节点为爷爷节点的左子树
     * |---情况4.2.1：插入节点为其父节点的左子节点（LL情况）
     *                  处理：父亲节点改为黑色，爷爷节点改为红色，对爷爷节点进行右旋
     * |---情况4.2.2：插入节点为其父节点的右子节点（LR情况）
     *                  处理：对父亲节点进行左旋，得到LL情况，按照LL情况继续处理。
     * |---情况4.3：叔叔节点不存在，或者为黑色，父节点为爷爷节点的右子树
     * |---情况4.3.1：插入节点为其父节点的右子节点（RR情况）
     *                  处理：父亲节点改为黑色，爷爷节点改为红色，对爷爷节点进行左旋
     * |---情况4.3.2：插入节点为其父节点的左子节点（RL情况）
     *                  处理：对父亲节点进行右旋，得到RR情况，按照RR情况继续处理。
     */
    private void insertFixUp(RBNode node) {
        RBNode parent = parentOf(node);
        RBNode gparent = parentOf(parent);
        //情况4：存在父节点且父节点为红色
        if (parent != null && isRed(parent)) {
            //父节点是红色的，那么一定存在爷爷节点

            //父节点为爷爷节点的左子树
            if (parent == gparent.left) {
                RBNode uncle = gparent.right;
                //情况4.1：叔叔节点存在，并且为红色（父-叔 双红）
                //将父和叔染色为黑色，再将爷爷染红，并将爷爷设置为当前节点，进入下一次循环判断
                if (uncle != null && isRed(uncle)) {
                    setBlack(parent);
                    setBlack(uncle);
                    setRed(gparent);
                    insertFixUp(gparent);
                    return;
                }

                //情况4.2：叔叔节点不存在，或者为黑色，父节点为爷爷节点的左子树
                if (uncle == null || isBlack(uncle)) {
                    //情况4.2.1：插入节点为其父节点的左子节点（LL情况）
                    //变色（父节点变黑，爷爷节点变红），右旋爷爷节点
                    if (node == parent.left) {
                        setBlack(parent);
                        setRed(gparent);
                        rightRotate(gparent);
                    }

                    //情况4.2.2：插入节点为其父节点的右子节点（LR情况）
                    //左旋（父节点）得到LL情况，当前节点设置为父节点，进入下一次循环
                    if (node == parent.right) {
                        leftRotate(parent);
                        insertFixUp(parent);
                        return;
                    }
                }
            } else {
                //父节点为爷爷节点的右子树
                RBNode uncle = gparent.left;
                //4.1：叔叔节点存在，并且为红色（父-叔 双红）
                //将父和叔染色为黑色，再将爷爷染红，并将爷爷设置为当前节点，进入下一次循环判断
                if (uncle != null && isRed(uncle)) {
                    setBlack(parent);
                    setBlack(uncle);
                    setRed(gparent);
                    insertFixUp(gparent);
                    return;
                }

                //情况4.3：叔叔节点不存在，或者为黑色，父节点为爷爷节点的右子树
                if (uncle == null || isBlack(uncle)) {
                    //情况4.3.1：插入节点为其父节点的右子节点（RR情况）
                    //变色（父节点变黑，爷爷节点变红），左旋爷爷节点
                    if (node == parent.right) {
                        setBlack(parent);
                        setRed(gparent);
                        leftRotate(gparent);
                    }

                    //情况4.3.2：插入节点为其父节点的左子节点（RL情况）
                    //右旋（父节点）得到RR情况，当前节点设置为父节点，进入下一次循环
                    if (node == parent.left) {
                        rightRotate(parent);
                        insertFixUp(parent);
                        return;
                    }
                }
            }
        }

        setBlack(this.root);
    }


    /**
     * 计算红黑树的高度
     * @param root 准备计算红黑树的根节点
     * @return
     */
    public static int getHeight(RBNode root) {
        if (root == null) {
            return 0;
        }
        return 1 + Math.max(getHeight(root.left), getHeight(root.right));
    }

    public RBNode findNode(String ipStart) {
        if(ipStart == null){
            return null;
        }
        RBNode node =root;
        while(node != null){
            if(node.ipStart.compareTo(ipStart) < 0){
                node = node.right;
            } else if (node.ipStart.compareTo(ipStart) == 0){
                return node;
            } else {
                node = node.left;
            }
        }
        return null;
    }

    private void clearNode(RBNode node){
        node.left = null;
        node.right = null;
        node.parent = null;
        node.ipStart = null;
        node = null;
    }

    /**
     * 左旋方法
     * 左旋示意图：左旋x节点
     * p                   p
     * |                   |
     * x                   y
     * / \         ---->   / \
     * lx  y               x   ry
     *    / \             / \
     *   ly  ry          lx  ly
     * <p>
     * 左旋做了几件事？
     * 1.将y的左子节点赋值给x的右边，并且把x设置为y的左子节点的父节点
     * 2.将x的父节点（非空时）指向y，更新y的父节点为x的父节点
     * 3.将y的左子节点指向x，更新x的父节点为y
     */
    private void leftRotate(RBNode x) {
        RBNode y = x.right;
        //将y的左子节点赋值给x的右边
        x.right = y.left;
        //并且把x设置为y的左子节点的父节点
        if (y.left != null) {
            y.left.parent = x;
        }

        //将x的父节点（非空时）指向y
        if (x.parent != null) {
            //如果x是parent左子树，则把y安放到parent的左边
            if (x.parent.left == x) {
                x.parent.left = y;
            } else {
                x.parent.right = y;
            }
            //更新y的父节点为x的父节点
            y.parent = x.parent;
        } else {
            this.root = y;
            this.root.parent = null;
        }

        //将y的左子节点指向x，更新x的父节点为y
        y.left = x;
        x.parent = y;
    }

    /**
     * 右旋方法
     * 右旋示意图：右旋y节点
     * <p>
     * p                       p
     * |                       |
     * y                       x
     * / \          ---->      / \
     * x   ry                  lx  y
     * / \                         / \
     * lx  ly                      ly  ry
     * <p>
     * 右旋都做了几件事？
     * 1.将x的右子节点 赋值 给了 y 的左子节点，并且更新x的右子节点的父节点为 y
     * 2.将y的父节点（不为空时）指向x，更新x的父节点为y的父节点
     * 3.将x的右子节点指向y，更新y的父节点为x
     */
    private void rightRotate(RBNode y) {
        RBNode x = y.left;
        //将x的右子节点赋值给y的左边
        y.left = x.right;
        //并且把y设置为x的右子节点的父节点
        if (x.right != null) {
            x.right.parent = y;
        }

        //将y的父节点p（非空时）指向x
        if (y.parent != null) {
            //如果y是parent左子树，则把x安放到parent的左边
            if (y.parent.left == y) {
                y.parent.left = x;
            } else {
                y.parent.right = x;
            }
            //更新x的父节点为y的父节点
            x.parent = y.parent;
        } else {
            this.root = x;
            this.root.parent = null;
        }

        //将x的右子节点赋值为y，将y的父节点设置为x
        x.right = y;
        y.parent = x;
    }

    /**
     * 获取当前节点的父节点
     */
    private RBNode parentOf(RBNode node) {
        if (node != null) {
            return node.parent;
        }
        return null;
    }

    /**
     * node节点是否为红色
     *
     * @return boolean true 表示是红色  false 表示不是红色
     */
    private boolean isRed(RBNode node) {
        if (node != null) {
            return node.isColor() == RED;
        }
        return false;
    }

    /**
     * node节点是否为黑色
     *
     * @return boolean true 表示是黑色  false 表示不是黑色
     */
    private boolean isBlack(RBNode node) {
        if (node != null) {
            return node.isColor() == BLACK;
        }
        return false;
    }

    /**
     * 设置节点为红色
     */
    private void setRed(RBNode node) {
        if (node != null) {
            node.setColor(RED);
        }
    }

    /**
     * 设置节点为黑色
     */
    private void setBlack(RBNode node) {
        if (node != null) {
            node.setColor(BLACK);
        }
    }

    /**
     * 中序打印，可以将二叉查找树有顺序的打印出来
     */
    public void inOrderPrint() {
        if (this.root != null) {
            inOrderPrint(this.root);
        }
    }

    private void inOrderPrint(RBNode node) {
        if (node != null) {
            inOrderPrint(node.left);
            System.out.println("ipStart -> " + node.ipStart + ", ipEnd -> " + node.ipEnd);
            inOrderPrint(node.right);
        }
    }

    /**
     * 红黑树Node
     */
    static class RBNode {
        //颜色
        private boolean color;
        //左子节点
        private RBNode left;
        //右子节点
        private RBNode right;
        //父节点
        private RBNode parent;
        //ipStart
        private String ipStart;
        //ipEnd
        private String ipEnd;

        public RBNode(boolean color, RBNode left, RBNode right, RBNode parent, String ipStart, String ipEnd) {
            this.color = color;
            this.left = left;
            this.right = right;
            this.parent = parent;
            this.ipStart = ipStart;
            this.ipEnd = ipEnd;
        }

        public RBNode() {
        }

        public boolean isColor() {
            return color;
        }

        public void setColor(boolean color) {
            this.color = color;
        }

        public RBNode getLeft() {
            return left;
        }

        public void setLeft(RBNode left) {
            this.left = left;
        }

        public RBNode getRight() {
            return right;
        }

        public void setRight(RBNode right) {
            this.right = right;
        }

        public RBNode getParent() {
            return parent;
        }

        public void setParent(RBNode parent) {
            this.parent = parent;
        }

        public String getIpStart() {
            return ipStart;
        }

        public void setIpStart(String ipStart) {
            this.ipStart = ipStart;
        }

        public String getIpEnd() {
            return ipEnd;
        }

        public void setIpEnd(String ipEnd) {
            this.ipEnd = ipEnd;
        }
    }
}
