/*
 * Abstract object renderer for image
 */
import java.awt.Color;
public class ObjRenderer {
    public Vec3[] localVerts;
    public int[] tris;
    public Color[] triColors;
    public Vec3 position;
    public Vec3 rotation; //in euler angle degrees
    public Vec3 scale;

    public Mat4x4 worldMatrix;
    public Vec4[] worldVerts;
    public Vec3[] normals;

    public ObjRenderer(){
        position = new Vec3();
        localVerts = new Vec3[0];
        tris = new int[0];
        triColors = new Color[0];
    }

    public void SetTransform(Vec3 position, Vec3 rotation, Vec3 scale){
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        UpdateWorldMatrix();
    }

    private void UpdateWorldMatrix(){
        
        //updates matrix for local to world coordinates
        //called as needed when object's transform is updated
        worldMatrix = new Mat4x4(new float[][]{
            //translation matrix
            {1, 0, 0, position.x},
            {0, 1, 0, position.y},
            {0, 0, 1, position.z},
            {0, 0, 0, 1}}
            );
        worldMatrix = worldMatrix.matProduct(new Mat4x4(new float[][]{
            //scale matrix
            {scale.x, 0, 0, 0},
            {0, scale.y, 0, 0},
            {0, 0, scale.z, 0},
            {0, 0, 0, 1}})
        );
        double xrad = rotation.x * Math.PI / 180;
        double yrad = rotation.y * Math.PI / 180;
        double zrad = rotation.z * Math.PI / 180;
        worldMatrix = worldMatrix.matProduct(new Mat4x4(new float[][]{
            //x rotation matrix
            {1, 0, 0, 0},
            {0, (float)Math.cos(xrad), -(float)Math.sin(xrad), 0},
            {0, (float)Math.sin(xrad), (float)Math.cos(xrad), 0},
            {0, 0, 0, 1}})
        );
        worldMatrix = worldMatrix.matProduct(new Mat4x4(new float[][]{
            //y rotation matrix
            {(float)Math.cos(yrad), 0, (float)Math.sin(yrad), 0},
            {0, 1, 0, 0},
            {-(float)Math.sin(yrad), 0, (float)Math.cos(yrad), 0},
            {0, 0, 0, 1}})
            );
            worldMatrix = worldMatrix.matProduct(new Mat4x4(new float[][]{
                //z rotation matrix
                {(float)Math.cos(zrad), -(float)Math.sin(zrad), 0, 0},
                {(float)Math.sin(zrad), (float)Math.cos(zrad), 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}})
                );
                
        //updating world vertices with new world matrix
        worldVerts = new Vec4[localVerts.length];
        for(int i = 0; i < localVerts.length; i++){
            worldVerts[i] = worldMatrix.v4product(localVerts[i]);
        }
        
        //setting normals here for now
        //tris go clockwise here, so v1v3 cross v1v2
        normals = new Vec3[tris.length / 3];
        for(int i = 0; i < tris.length; i += 3){
            Vec3 v1v3 = new Vec3(
                worldVerts[tris[i + 2]].x - worldVerts[tris[i]].x,
                worldVerts[tris[i + 2]].y - worldVerts[tris[i]].y,
                worldVerts[tris[i + 2]].z - worldVerts[tris[i]].z);
            Vec3 v1v2 = new Vec3(
                worldVerts[tris[i + 1]].x - worldVerts[tris[i]].x,
                worldVerts[tris[i + 1]].y - worldVerts[tris[i]].y,
                worldVerts[tris[i + 1]].z - worldVerts[tris[i]].z);
            normals[i / 3] = v1v2.cross(v1v3);
            normals[i / 3].Normalize();
        }
    };
}