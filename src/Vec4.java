/*
 * Four dimensional vector class with common functions for the geometric space
 * Contains quaternion multiplication(composition) and inverse functions
 */
public class Vec4{
    public float x, y, z, w;
    public Vec4(float x, float y, float z, float w){
        this.x = x; this.y = y; this.z = z; this.w = w;
    }
    public Vec4(Vec3 v3){
        this.x = v3.x; this.y = v3.y; this.z = v3.z; this.w = 1;
    }
    public Vec4(){
        x = y = z = w = 0;
    }
    public Vec4 QuaternionMultiply(Vec4 t){
        Vec3 v1 = new Vec3(y, z, w);
        Vec3 v2 = new Vec3(t.y, t.z, t.w);
        Vec3 cross = v1.cross(v2);
        return new Vec4(
            x * t.x - (float)v1.dot(v2),
            x * t.y + t.x * y + cross.x,
            x * t.z + t.x * z + cross.y,
            x * t.w + t.x * w + cross.z
        );
    }
    public Vec4 QuaternionInverse(){
        float normsq = (float)(x * x + y * y + z * z + w * w);
        return new Vec4(
            x / normsq, -y / normsq, -z / normsq, -w / normsq
        );
    }
    public void Normalize(){
        float mag = (float)Math.sqrt(x * x + y * y + z * z + w * w);
        x /= mag; y /= mag; z /= mag; w /= mag;
    }
    public Vec3 toVec3(){
        return new Vec3(x, y, z);
    }
    @Override
    public String toString() {
        return x + ", " + y + ", " + z + ", " + w;
    }
}