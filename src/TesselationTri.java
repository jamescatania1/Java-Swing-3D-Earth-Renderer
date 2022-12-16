/*
 * Triangles which dynamically tesselate in steps of 1 -> 16
 * Tesselation conditions are handled in the MapGraphics class
 * during paintComponent() calls
 */
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
public class TesselationTri extends ObjRenderer{
    public TesselationTri[] subTris;
    public Vec3 mPoint;
    public boolean isLeaf;
    public boolean canTesselate;
    public int depth;
    public TesselationTri(Vec3 v1, Vec3 v2, Vec3 v3, int depth){ //creates leaf tri node
        isLeaf = true;
        this.depth = depth;
        canTesselate = depth < EarthIcosphere.MAX_TESSELATION_DEPTH;
        subTris = null;
        mPoint = new Vec3(
            (v1.x + v2.x + v3.x) / 3f, 
            (v1.y + v2.y + v3.y) / 3f, 
            (v1.z + v2.z + v3.z) / 3f);
        localVerts = new Vec3[]{v1, v2, v3};
        tris = new int[]{0, 1, 2};
        triColors = new Color[]{Color.BLUE, Color.BLUE, Color.BLUE};
        Subdivide(); Subdivide();
        SetTransform(new Vec3(), new Vec3(), new Vec3(1, 1, 1));
    }
    public void Tesselate(){
        isLeaf = false;
        if(subTris != null) return;
        subTris = new TesselationTri[16];
        for(int i = 0; i < tris.length; i+=3){
            subTris[i / 3] = new TesselationTri(localVerts[tris[i]],localVerts[tris[i + 1]],localVerts[tris[i + 2]], depth + 1);
        }
    }
    public void UnTesselate(){
        subTris = null;
        isLeaf = true;
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
            m01.NormalizeTo(EarthIcosphere.EARTH_RADIUS); 
            m12.NormalizeTo(EarthIcosphere.EARTH_RADIUS); 
            m02.NormalizeTo(EarthIcosphere.EARTH_RADIUS);
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
        triColors = new Color[tris.length / 3];
        for(int i = 0; i < triColors.length; i++) {
            triColors[i] = EarthIcosphere.PtColor(new Vec3(
                localVerts[tris[i * 3]].x + localVerts[tris[i * 3 + 1]].x + localVerts[tris[i * 3 + 2]].x / 3,
                localVerts[tris[i * 3]].y + localVerts[tris[i * 3 + 1]].y + localVerts[tris[i * 3 + 2]].y / 3,
                localVerts[tris[i * 3]].z + localVerts[tris[i * 3 + 1]].z + localVerts[tris[i * 3 + 2]].z / 3));
        }
    }
}