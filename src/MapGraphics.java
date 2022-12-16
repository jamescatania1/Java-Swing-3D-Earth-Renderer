/*
 * 3D graphics drawing using icosphere, recursive tesselated rendering,
 * and backface culling. Drawing method uses painter's algorithm and
 * does not use pixel testing. Hence, there is no texture data and they
 * are using per-triangle colors
 * PRESS SPACE FOR FPS/TRI COUNT OF FRAME
 */
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
public class MapGraphics extends JFrame {
    private final int TARGET_FPS = 120;
    private final boolean VERT_GIZMOS = false;
    private final float TESSELATE_SCPROP = .03f;
    public double deltaTime;
    public int triCt;
    public int fps;
    public int width;
    public int height;
    private long timeCtr = 0;
    private ArcballCam camera;
    private EarthIcosphere earth;
    public static void main(String[] args) {
        new MapGraphics();
    }
    public MapGraphics(){
        setTitle("Street Map");
        setVisible(true);
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(800, 600));
        
        MapPanel mainPanel = new MapPanel();
        add(mainPanel);
        width = mainPanel.getWidth();
        height = mainPanel.getHeight();

        earth = new EarthIcosphere();

        camera = new ArcballCam(this);
        addKeyListener(camera);
        addMouseListener(camera);
        addMouseMotionListener(camera);
        addMouseWheelListener(camera);

        timeCtr = System.nanoTime();
        while(true){ //update/render loop
            if(System.nanoTime() - timeCtr > 1000000000 / TARGET_FPS){
                deltaTime = (double)(System.nanoTime() - timeCtr) / 1000000000d;
                fps = (int)(1f / deltaTime);
                timeCtr = System.nanoTime();
                width = mainPanel.getWidth();
                height = mainPanel.getHeight();
                camera.Update();
                repaint();
            }
        } 
    }

    private class MapPanel extends JPanel {
        public MapPanel(){
            setBackground(Color.BLACK);
        }
        @Override
        protected void paintComponent(Graphics g) {
            //clearing Frame
            int width = getWidth();
            int height = getHeight();
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, width, height);
            
            if(camera == null || camera.viewProjMatrix == null) return;
            
            int[] tTriIndicies = new int[earth.tessTris.size()];
            float[] tTriDistances = new float[earth.tessTris.size()];
            for(int i = 0; i < earth.tessTris.size(); i++){
                tTriIndicies[i] = i;
                tTriDistances[i] = camera.viewProjMatrix.v4product(earth.tessTris.get(i).mPoint).z;
            }
            QSTris(tTriIndicies, tTriDistances, 0, tTriIndicies.length - 1);
            triCt = 0;
            for(int i = 0; i < tTriDistances.length; i++){
                RenderTessTri(earth.tessTris.get(tTriIndicies[i]), g);
            }
        }
        
        private void QSTris(int[] in, float[] compare, int lowIndx, int hiIndx){
            //quicksort algorithm based on distance val per tri, sorting method for painter's algorithm
            if(lowIndx >= hiIndx) return; //Base case
            int lowIndxEnd = QSPartition(in, compare, lowIndx, hiIndx);
            QSTris(in, compare, lowIndx, lowIndxEnd);
            QSTris(in, compare, lowIndxEnd + 1, hiIndx);
        }
        private int QSPartition(int[] in, float[] compare, int lowIndx, int hiIndx){
            int midindx = lowIndx + (hiIndx - lowIndx) / 2;
            float pivot = compare[midindx];
            while(true){
                while(compare[lowIndx] > pivot) lowIndx++;
                while(compare[hiIndx] < pivot) hiIndx--;
                if(lowIndx >= hiIndx) break;
                else{
                    int tmp = in[lowIndx];
                    in[lowIndx] = in[hiIndx];
                    in[hiIndx] = tmp;
                    float ctmp = compare[lowIndx];
                    compare[lowIndx] = compare[hiIndx];
                    compare[hiIndx] = ctmp;
                    lowIndx++;
                    hiIndx--;
                }
            }
            return hiIndx;
        }
        public void RenderTessTri(TesselationTri tri, Graphics g){
            if(!tri.isLeaf){ //internal node
                if(tri.subTris == null) return;
                int[] sTriIndicies = new int[tri.subTris.length];
                float[] sTriDistances = new float[tri.subTris.length];
                for(int i = 0; i < sTriIndicies.length; i++){
                    sTriIndicies[i] = i;
                    sTriDistances[i] = camera.viewProjMatrix.v4product(tri.subTris[i].mPoint).z;
                }
                QSTris(sTriIndicies, sTriDistances, 0, sTriIndicies.length - 1);
                for(int i = 0; i < sTriIndicies.length; i++){
                    RenderTessTri(tri.subTris[sTriIndicies[i]], g);
                }

                if(tri.subTris[0].isLeaf){ //internal nodes penultimate to leaves
                    //finding screen space amt that tri takes up, untesselating if too small
                    Vec4 p1 = camera.viewProjMatrix.v4product(tri.worldVerts[0]);
                    Vec4 p2 = camera.viewProjMatrix.v4product(tri.worldVerts[1]);
                    Vec4 p3 = camera.viewProjMatrix.v4product(tri.worldVerts[2]);
                    float wDim = Math.min(1f, ((float)width) / (float)height);
                    float hDim = Math.max(1f, ((float)height) / (float)width);
                    Vec3 sc1 = new Vec3((p1.x / p1.w) / wDim, (p1.y / p1.w) * hDim, p1.z);
                    Vec3 sc2 = new Vec3((p2.x / p2.w) / wDim, (p2.y / p2.w) * hDim, p2.z);
                    Vec3 sc3 = new Vec3((p3.x / p3.w) / wDim, (p3.y / p3.w) * hDim, p3.z);
                    float x1 = Math.min(width, Math.max(0, (float)((sc1.x + 1) * .5 * width))); 
                    float y1 = Math.min(height, Math.max(0, (float)((sc1.y + 1) * .5 * height))); 
                    float x2 = Math.min(width, Math.max(0, (float)((sc2.x + 1) * .5 * width))); 
                    float y2 = Math.min(height, Math.max(0, (float)((sc2.y + 1) * .5 * height))); 
                    float x3 = Math.min(width, Math.max(0, (float)((sc3.x + 1) * .5 * width))); 
                    float y3 = Math.min(height, Math.max(0, (float)((sc3.y + 1) * .5 * height))); 
                    float scProp = Math.abs((x3-x1)*(y2-y1) - (y3-y1)*(x2-x1)) / 2f;
                    scProp /= (float)(width * height);
                    if(scProp < TESSELATE_SCPROP) tri.UnTesselate();
                }
                return;
            }
            //leaf node base case: render mesh data
            Vec3[] scVerts = new Vec3[tri.worldVerts.length];
            int[] pixVertsX = new int[tri.worldVerts.length];
            int[] pixVertsY = new int[tri.worldVerts.length];
            float wDim = Math.min(1f, ((float)width) / (float)height);
            float hDim = Math.max(1f, ((float)height) / (float)width);
            for(int i = 0; i < scVerts.length; i ++){
                //setting screen space and pixel space vertex coords
                Vec4 prod = camera.viewProjMatrix.v4product(tri.worldVerts[i]);
                scVerts[i] = new Vec3((prod.x / prod.w) / wDim, (prod.y / prod.w) * hDim, prod.z);
                pixVertsX[i] = (int)((scVerts[i].x + 1) * .5 * width);
                pixVertsY[i] = (int)((scVerts[i].y + 1) * .5 * height);
            }

            //painter's algorithm sorting
            //tried using zbuffer, but was too slow
            int[] triIndicies = new int[tri.tris.length / 3];
            float[] distanceVals = new float[tri.tris.length];
            for(int i = 0; i < tri.tris.length; i += 3){
                distanceVals[i / 3] = (scVerts[tri.tris[i]].z + scVerts[tri.tris[i+ 1]].z + scVerts[tri.tris[i + 2]].z) / 3f;
                triIndicies[i / 3] = i / 3;
            }
            for(int i = 0; i < triIndicies.length; i++) triIndicies[i] = i;
            QSTris(triIndicies, distanceVals, 0, triIndicies.length - 1);

            for(int i = 0; i < tri.tris.length; i += 3){
                int indx = triIndicies[i / 3] * 3;
                if((Math.abs(scVerts[tri.tris[indx]].x) > 1.1f || Math.abs(scVerts[tri.tris[indx]].y) > 1.1f) 
                && (Math.abs(scVerts[tri.tris[indx + 1]].x) > 1.1f || Math.abs(scVerts[tri.tris[indx + 1]].y) > 1.1f) 
                && (Math.abs(scVerts[tri.tris[indx + 2]].x) > 1.1f || Math.abs(scVerts[tri.tris[indx + 2]].y) > 1.1f)){
                    //tri is outside of the screen bounds, cull
                    //individual tri culling works here, as skipping tris is fine with cpu,
                    //and g.drawPolygon() is so slow that it is beneficial
                    continue;
                }
                Vec3 cVec = camera.position.subtract(tri.worldVerts[tri.tris[indx]].toVec3());
                cVec.Normalize();
                if(cVec.dot(tri.normals[indx / 3]) > 0f){ //backface culling, using clockwise tri direction
                    //dot camera normal with triangle normal, cull if positive
                    continue;
                }
                g.setColor(tri.triColors[indx / 3]);
                g.fillPolygon(
                    new int[]{
                        pixVertsX[tri.tris[indx]], pixVertsX[tri.tris[indx + 1]], pixVertsX[tri.tris[indx + 2]]
                    }, new int[]{
                        pixVertsY[tri.tris[indx]], pixVertsY[tri.tris[indx + 1]], pixVertsY[tri.tris[indx + 2]]
                    }, 3);
                triCt++;
            }
            if(VERT_GIZMOS){ //draw circles for vertex debugging
                int gizmoDiam = 2;
                g.setColor(Color.YELLOW);
                for(int i = 0; i < scVerts.length; i ++){
                    g.fillOval(pixVertsX[i] - gizmoDiam / 2, pixVertsY[i] - gizmoDiam / 2,
                     gizmoDiam, gizmoDiam);
                }
            }

            //finding screen space amt that tri takes up, tesselating if tri of adequate size
            int x1 = Math.min(width, Math.max(0, pixVertsX[0])); int y1 = Math.min(height, Math.max(0, pixVertsY[0]));
            int x2 = Math.min(width, Math.max(0, pixVertsX[1])); int y2 = Math.min(height, Math.max(0, pixVertsY[1]));
            int x3 = Math.min(width, Math.max(0, pixVertsX[2])); int y3 = Math.min(height, Math.max(0, pixVertsY[2]));
            Vec3 v1v3 = new Vec3((float)(x3-x1), (float)(y3-y1), 0f);
            Vec3 v1v2 = new Vec3((float)(x2-x1), (float)(y2-y1), 0f);
            float scProp = Math.abs(v1v3.x*v1v2.y - v1v3.y*v1v2.x) / 2f;
            scProp /= (float)(width * height);
            if(scProp > TESSELATE_SCPROP && tri.canTesselate) tri.Tesselate();
        }
    }
}