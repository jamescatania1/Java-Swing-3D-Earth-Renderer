/*
 * Earth image used from Nasa's website
 * https://visibleearth.nasa.gov/images/73826/october-blue-marble-next-generation-w-topography-and-bathymetry/73835l
 */
import java.io.File;
import java.io.IOException;
import java.awt.Color;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
public class EarthIcosphere{
    public static final float EARTH_RADIUS = 6378100; //in km
    public static final int MAX_TESSELATION_DEPTH = 7;
    public static BufferedImage earthImg = null;
    public ArrayList<TesselationTri> tessTris = new ArrayList<>();
    Vec3[] localVerts;
    int[] tris;
    public EarthIcosphere(){
        //loading image to memory
        try {
            earthImg = ImageIO.read(new File("worldmap.png"));
        } catch (IOException e) { e.printStackTrace(); }
        
        float x = .525731112119133606f; //these are normalized already
        float z = .850650808352039932f;
        x *= EARTH_RADIUS; z *= EARTH_RADIUS;
        /* 
         * verts/tris found from OpenGL tutorial
         * Untitled document. OpenGL. (n.d.). Retrieved December 8, 2022, 
         * from https://opengl.org.ru/docs/pg/0208.html 
        */
        localVerts = new Vec3[]{
            new Vec3(-x, 0, z), new Vec3(x, 0, z), new Vec3(-x, 0, -z),
            new Vec3(x, 0, -z), new Vec3(0, z, x), new Vec3(0, z, -x),
            new Vec3(0, -z, x), new Vec3(0, -z, -x), new Vec3(z, x, 0),
            new Vec3(-z, x, 0), new Vec3(z, -x, 0), new Vec3(-z, -x, 0)
        };
        tris = new int[]{
            0, 4, 1,    0, 9, 4,    9, 5, 4,    4, 5, 8,
            4, 8, 1,    8, 10, 1,   8, 3, 10,   5, 3, 8,
            5, 2, 3,    2, 7, 3,    7, 10, 3,   7, 6, 10,
            7, 11, 6,   11, 0, 6,   0, 1, 6,    6, 1, 10,
            9, 0, 11,   9, 11, 2,   9, 2, 5,    7, 2, 11
        };
        Subdivide(); Subdivide(); Subdivide();
        for(int i = 0; i < tris.length; i += 3){
            tessTris.add(new TesselationTri(localVerts[tris[i]], localVerts[tris[i + 1]], localVerts[tris[i + 2]], 1));
        }
    }
    public static Color PtColor(Vec3 point){
        /*
        * This imgae uses the plate caree equidirecangular projection, recovering latitude and longitude via this wiki article:
        * Wikipedia contributors. (2022, September 6). Equirecection. In Wikipedia, The Free Encyclopedia. Retrieved 20:37, December 12, 2022, 
        * from https://en.wikipedia.org/w/index.php?title=Equirectangular_projection&oldid=1108776583
        */
        point.Normalize();
        double theta = Math.atan(point.y / point.x) / Math.PI + 1d; //longitude, yaw
        double fi = Math.atan(Math.sqrt(point.x * point.x + point.y * point.y) / point.z) / Math.PI + 1d; //latitude, pitch
        Vec3 px = PxPt(theta, fi);
        return new Color(earthImg.getRGB((int)px.x, (int)px.y));
    }
    public static Vec3 PxPt(double theta, double fi){
        int x = ((int)((double)earthImg.getWidth() * theta)) % earthImg.getWidth();
        int y = ((int)((double)earthImg.getHeight() * fi)) % earthImg.getHeight();
        return new Vec3(x, y, 0);
    }

    private void Subdivide(){
        int n = localVerts.length;
        ArrayList<Vec3> nVerts = new ArrayList<>(n * 4);
        HashMap<Vec3, Integer> vertIndicies = new HashMap<>();
        int[] nTris = new int[tris.length * 4];
        for(int i = 0; i < n ; i++) nVerts.add(localVerts[i]);
        for(int i = 0; i < tris.length; i += 3){
            Vec3 m01 = localVerts[tris[i]].midpoint(localVerts[tris[i + 1]]);
            Vec3 m12 = localVerts[tris[i + 1]].midpoint(localVerts[tris[i + 2]]);
            Vec3 m02 = localVerts[tris[i]].midpoint(localVerts[tris[i + 2]]);
            m01.NormalizeTo(EARTH_RADIUS); m12.NormalizeTo(EARTH_RADIUS); m02.NormalizeTo(EARTH_RADIUS);
            int vm01 = 0, vm12 = 0, vm02 = 0;
            if(!vertIndicies.containsKey(m01)){
                nVerts.add(m01);  
                vertIndicies.put(m01, nVerts.size() - 1);
                vm01 = nVerts.size() - 1;
            }
            if(!vertIndicies.containsKey(m12)){
                nVerts.add(m12);
                vertIndicies.put(m12, nVerts.size() - 1);
                vm12 = nVerts.size() - 1;
            }
            if(!vertIndicies.containsKey(m02)){
                nVerts.add(m02);
                vertIndicies.put(m02, nVerts.size() - 1);
                vm02 = nVerts.size() - 1;
            }
            if(vm01 == 0) vm01 = vertIndicies.get(m01);
            if(vm12 == 0) vm12 = vertIndicies.get(m12);
            if(vm02 == 0) vm02 = vertIndicies.get(m02);
            nTris[i * 4 + 0] = tris[i + 0];     nTris[i * 4 + 1] = vm01;    nTris[i * 4 + 2] = vm02;
            nTris[i * 4 + 3] = tris[i + 1];     nTris[i * 4 + 4] = vm12;    nTris[i * 4 + 5] = vm01;
            nTris[i * 4 + 6] = tris[i + 2];     nTris[i * 4 + 7] = vm02;    nTris[i * 4 + 8] = vm12;
            nTris[i * 4 + 9] = vm01;            nTris[i * 4 + 10] = vm12;   nTris[i * 4 + 11] = vm02; 
        }
        localVerts = new Vec3[nVerts.size()];
        localVerts = nVerts.toArray(localVerts);
        tris = nTris;
    }
}