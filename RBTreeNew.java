package com.dp.dnsc.util;

import com.dp.plat.type.IpAddrNg;

/**
 * �ٴ���RBTree��������ɫ
 * �ڴ���RBNode
 * �۸����������壺parentOf(node)��isRed(node)��setRed(node)��setBlack(node)��inOrderPrint(RBNode tree)
 * �������������壺leftRotate(node)
 * �������������壺rightRotate(node)
 * �޹�������ӿڷ������壺insert(String ipStrat, String ipEnd);
 * ���ڲ�����ӿڷ������壺insert(RBNode node);
 * ���������뵼�º����ʧ��ķ������壺insertFIxUp(RBNode node);
 * ����Ժ������ȷ��
 *
 *
 * ������������������ڶԺ������ݵ�IP���ظ��Ƚ�
 * @date 2024/2/21
 * @author l05455 ����
 */
public class RBTreeNew {

    //������ɫ����
    private static final boolean RED = true;
    private static final boolean BLACK = false;

    //����ʧ�ܱ�ʶ
    public static boolean insertFlag = true;

    //�����������
    private RBNode root;

    public RBNode getRoot() {
        return root;
    }

    /**
     * �����Ĳ���ӿ�
     *
     * @param ipStart   ��ʼIP
     * @param ipEnd     ����IP
     */
    public String insert(String ipStart, String ipEnd) {
        RBNode node = new RBNode();
        String res; //���ز���ɹ���ʧ�ܵĽ�����ɹ�Ϊ""��ʧ��Ϊ����IP��
        node.setIpStart(ipStart);
        node.setIpEnd(ipEnd);
        node.setColor(RED);
        res = insert(node);
//        System.out.println(insertFlag);
        return res;
    }

    /**
     * �ڲ�����ӿڶ���
     */
    private String insert(RBNode node) {
        //1.�ҵ������λ��
        RBNode parent = null;
        RBNode x = this.root; //x��Ϊÿ�ζԺ�������������Ľڵ㣬ֱ���ҵ��ڵ�Ϊ��ʱ����ɶ��½ڵ���в��붯��
        while (x != null) {
            parent = x;

            //�ֱ�Բ������ʼIP�ͽ���IP �� �п�����Ϊ�丸�ڵ�Ľڵ� ���Ƚ�
            //��������ظ���IP���н��棬�򷵻ش�����Ϣ
            String ipStart = node.ipStart;
            String ipEnd = node.ipEnd;
            String parentIpStart = parent.ipStart;
            String parentIpEnd = parent.ipEnd;

            int ipStartcmpA = ipStart.compareTo(parentIpStart);
            int ipStartcmpB = ipEnd.compareTo(parentIpStart);
            int ipEndcmpA = ipStart.compareTo(parentIpEnd);
            int ipEndcmpB = ipEnd.compareTo(parentIpEnd);

            if (ipStartcmpA < 0 && ipStartcmpB < 0) {
                //ȫ��<0Ϊ����ͨ�����ҵ�ǰԤ������ڵ��IP��С�ڵ�ǰ�ĸ��ڵ㣬������ȡ�˸��ڵ������
                x = x.left;
            } else if (ipEndcmpA > 0 && ipEndcmpB > 0) {
                //ȫ��>0Ϊ����ͨ�����ҵ�ǰԤ������ڵ��IP�δ��ڵ�ǰ�ĸ��ڵ㣬������ȡ�˸��ڵ���Һ���
                x = x.right;
            } else {
                //���������Ϊ�ж�ʧ�ܣ����ش�����Ϣ--���ڳ�ͻ��IP��
                insertFlag = false;
//                System.out.println("IP��С��ͻ����");
                String res = new IpAddrNg(parent.ipStart).formatTo() + "-" + new IpAddrNg(parent.ipEnd).formatTo();
                return res;
            }
        }

        //��x�ڵ�����ӻ��Һ���Ϊnullʱ��֤��x�ڵ������Ϊ�������½ڵ�ĸ��ڵ㣬�����ж���Ϊ���ӻ����Һ���
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

        //����֮����Ҫ�����޸���������ú�����ٴ�ƽ��
        insertFixUp(node);
        return "";
    }

    /**
     * ������޸������ƽ��ķ�������������ۣ�
     * |---���1�������Ϊ����
     *              ����ֱ�ӰѲ�������Ϊ����㣬���ڵ㸳��ɫ��
     * |---���2������ڵ��IP���н���
     *              �������ز���ʧ����Ϣ--����ʧ��IP�Ρ�
     * |---���3������ڵ�ĸ��ڵ�Ϊ��ɫ
     *              �����Բ���ڵ㸳��ɫ��ֱ�Ӳ��뼴�ɡ�
     * |---���4������ڵ�ĸ��ڵ�Ϊ��ɫ
     * |---���4.1������ڵ���ڣ�����Ϊ��ɫ����-�� ˫�죩
     *                �������׺�����ڵ��Ϊ��ɫ��үү�ڵ��Ϊ��ɫ����үү�ڵ���Ϊ��ǰ�ڵ����ƽ�⴦��
     * |---���4.2������ڵ㲻���ڣ�����Ϊ��ɫ�����ڵ�Ϊүү�ڵ��������
     * |---���4.2.1������ڵ�Ϊ�丸�ڵ�����ӽڵ㣨LL�����
     *                  �������׽ڵ��Ϊ��ɫ��үү�ڵ��Ϊ��ɫ����үү�ڵ��������
     * |---���4.2.2������ڵ�Ϊ�丸�ڵ�����ӽڵ㣨LR�����
     *                  �����Ը��׽ڵ�����������õ�LL���������LL�����������
     * |---���4.3������ڵ㲻���ڣ�����Ϊ��ɫ�����ڵ�Ϊүү�ڵ��������
     * |---���4.3.1������ڵ�Ϊ�丸�ڵ�����ӽڵ㣨RR�����
     *                  �������׽ڵ��Ϊ��ɫ��үү�ڵ��Ϊ��ɫ����үү�ڵ��������
     * |---���4.3.2������ڵ�Ϊ�丸�ڵ�����ӽڵ㣨RL�����
     *                  �����Ը��׽ڵ�����������õ�RR���������RR�����������
     */
    private void insertFixUp(RBNode node) {
        RBNode parent = parentOf(node);
        RBNode gparent = parentOf(parent);
        //���4�����ڸ��ڵ��Ҹ��ڵ�Ϊ��ɫ
        if (parent != null && isRed(parent)) {
            //���ڵ��Ǻ�ɫ�ģ���ôһ������үү�ڵ�

            //���ڵ�Ϊүү�ڵ��������
            if (parent == gparent.left) {
                RBNode uncle = gparent.right;
                //���4.1������ڵ���ڣ�����Ϊ��ɫ����-�� ˫�죩
                //��������ȾɫΪ��ɫ���ٽ�үүȾ�죬����үү����Ϊ��ǰ�ڵ㣬������һ��ѭ���ж�
                if (uncle != null && isRed(uncle)) {
                    setBlack(parent);
                    setBlack(uncle);
                    setRed(gparent);
                    insertFixUp(gparent);
                    return;
                }

                //���4.2������ڵ㲻���ڣ�����Ϊ��ɫ�����ڵ�Ϊүү�ڵ��������
                if (uncle == null || isBlack(uncle)) {
                    //���4.2.1������ڵ�Ϊ�丸�ڵ�����ӽڵ㣨LL�����
                    //��ɫ�����ڵ��ڣ�үү�ڵ��죩������үү�ڵ�
                    if (node == parent.left) {
                        setBlack(parent);
                        setRed(gparent);
                        rightRotate(gparent);
                    }

                    //���4.2.2������ڵ�Ϊ�丸�ڵ�����ӽڵ㣨LR�����
                    //���������ڵ㣩�õ�LL�������ǰ�ڵ�����Ϊ���ڵ㣬������һ��ѭ��
                    if (node == parent.right) {
                        leftRotate(parent);
                        insertFixUp(parent);
                        return;
                    }
                }
            } else {
                //���ڵ�Ϊүү�ڵ��������
                RBNode uncle = gparent.left;
                //4.1������ڵ���ڣ�����Ϊ��ɫ����-�� ˫�죩
                //��������ȾɫΪ��ɫ���ٽ�үүȾ�죬����үү����Ϊ��ǰ�ڵ㣬������һ��ѭ���ж�
                if (uncle != null && isRed(uncle)) {
                    setBlack(parent);
                    setBlack(uncle);
                    setRed(gparent);
                    insertFixUp(gparent);
                    return;
                }

                //���4.3������ڵ㲻���ڣ�����Ϊ��ɫ�����ڵ�Ϊүү�ڵ��������
                if (uncle == null || isBlack(uncle)) {
                    //���4.3.1������ڵ�Ϊ�丸�ڵ�����ӽڵ㣨RR�����
                    //��ɫ�����ڵ��ڣ�үү�ڵ��죩������үү�ڵ�
                    if (node == parent.right) {
                        setBlack(parent);
                        setRed(gparent);
                        leftRotate(gparent);
                    }

                    //���4.3.2������ڵ�Ϊ�丸�ڵ�����ӽڵ㣨RL�����
                    //���������ڵ㣩�õ�RR�������ǰ�ڵ�����Ϊ���ڵ㣬������һ��ѭ��
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
     * ���������ĸ߶�
     * @param root ׼�����������ĸ��ڵ�
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
     * ��������
     * ����ʾ��ͼ������x�ڵ�
     * p                   p
     * |                   |
     * x                   y
     * / \         ---->   / \
     * lx  y               x   ry
     *    / \             / \
     *   ly  ry          lx  ly
     * <p>
     * �������˼����£�
     * 1.��y�����ӽڵ㸳ֵ��x���ұߣ����Ұ�x����Ϊy�����ӽڵ�ĸ��ڵ�
     * 2.��x�ĸ��ڵ㣨�ǿ�ʱ��ָ��y������y�ĸ��ڵ�Ϊx�ĸ��ڵ�
     * 3.��y�����ӽڵ�ָ��x������x�ĸ��ڵ�Ϊy
     */
    private void leftRotate(RBNode x) {
        RBNode y = x.right;
        //��y�����ӽڵ㸳ֵ��x���ұ�
        x.right = y.left;
        //���Ұ�x����Ϊy�����ӽڵ�ĸ��ڵ�
        if (y.left != null) {
            y.left.parent = x;
        }

        //��x�ĸ��ڵ㣨�ǿ�ʱ��ָ��y
        if (x.parent != null) {
            //���x��parent�����������y���ŵ�parent�����
            if (x.parent.left == x) {
                x.parent.left = y;
            } else {
                x.parent.right = y;
            }
            //����y�ĸ��ڵ�Ϊx�ĸ��ڵ�
            y.parent = x.parent;
        } else {
            this.root = y;
            this.root.parent = null;
        }

        //��y�����ӽڵ�ָ��x������x�ĸ��ڵ�Ϊy
        y.left = x;
        x.parent = y;
    }

    /**
     * ��������
     * ����ʾ��ͼ������y�ڵ�
     * <p>
     * p                       p
     * |                       |
     * y                       x
     * / \          ---->      / \
     * x   ry                  lx  y
     * / \                         / \
     * lx  ly                      ly  ry
     * <p>
     * ���������˼����£�
     * 1.��x�����ӽڵ� ��ֵ ���� y �����ӽڵ㣬���Ҹ���x�����ӽڵ�ĸ��ڵ�Ϊ y
     * 2.��y�ĸ��ڵ㣨��Ϊ��ʱ��ָ��x������x�ĸ��ڵ�Ϊy�ĸ��ڵ�
     * 3.��x�����ӽڵ�ָ��y������y�ĸ��ڵ�Ϊx
     */
    private void rightRotate(RBNode y) {
        RBNode x = y.left;
        //��x�����ӽڵ㸳ֵ��y�����
        y.left = x.right;
        //���Ұ�y����Ϊx�����ӽڵ�ĸ��ڵ�
        if (x.right != null) {
            x.right.parent = y;
        }

        //��y�ĸ��ڵ�p���ǿ�ʱ��ָ��x
        if (y.parent != null) {
            //���y��parent�����������x���ŵ�parent�����
            if (y.parent.left == y) {
                y.parent.left = x;
            } else {
                y.parent.right = x;
            }
            //����x�ĸ��ڵ�Ϊy�ĸ��ڵ�
            x.parent = y.parent;
        } else {
            this.root = x;
            this.root.parent = null;
        }

        //��x�����ӽڵ㸳ֵΪy����y�ĸ��ڵ�����Ϊx
        x.right = y;
        y.parent = x;
    }

    /**
     * ��ȡ��ǰ�ڵ�ĸ��ڵ�
     */
    private RBNode parentOf(RBNode node) {
        if (node != null) {
            return node.parent;
        }
        return null;
    }

    /**
     * node�ڵ��Ƿ�Ϊ��ɫ
     *
     * @return boolean true ��ʾ�Ǻ�ɫ  false ��ʾ���Ǻ�ɫ
     */
    private boolean isRed(RBNode node) {
        if (node != null) {
            return node.isColor() == RED;
        }
        return false;
    }

    /**
     * node�ڵ��Ƿ�Ϊ��ɫ
     *
     * @return boolean true ��ʾ�Ǻ�ɫ  false ��ʾ���Ǻ�ɫ
     */
    private boolean isBlack(RBNode node) {
        if (node != null) {
            return node.isColor() == BLACK;
        }
        return false;
    }

    /**
     * ���ýڵ�Ϊ��ɫ
     */
    private void setRed(RBNode node) {
        if (node != null) {
            node.setColor(RED);
        }
    }

    /**
     * ���ýڵ�Ϊ��ɫ
     */
    private void setBlack(RBNode node) {
        if (node != null) {
            node.setColor(BLACK);
        }
    }

    /**
     * �����ӡ�����Խ������������˳��Ĵ�ӡ����
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
     * �����Node
     */
    static class RBNode {
        //��ɫ
        private boolean color;
        //���ӽڵ�
        private RBNode left;
        //���ӽڵ�
        private RBNode right;
        //���ڵ�
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
