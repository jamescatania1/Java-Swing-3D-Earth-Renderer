/*
 * Three dimensional vector class with common functions for the geometric space
 */
public class Vec3 {
    public float x, y, z;
    public Vec3(float x, float y, float z){
        this.x = x; this.y = y; this.z = z;
    }
    public Vec3(){
        x = y = z = 0;
        
    }
    public void Normalize(){
        double mag = Math.sqrt(x * x + y * y + z * z);
        x /= mag; y /= mag; z /= mag;
    }
    public void NormalizeTo(float magnitude){
        Normalize();
        x *= magnitude; y *= magnitude; z *= magnitude;
    }
    public double Magnitude(){
        return Math.sqrt(x * x + y * y + z * z);
    }
    public Vec3 cross(Vec3 t){
        return new Vec3(
            y * t.z - z * t.y,
            z * t.x - x * t.z,
            x * t.y - y * t.x);
    }
    public double dot(Vec3 t){
        return x * t.x + y * t.y + z * t.z;
    }
    public Vec3 midpoint(Vec3 t){
        return new Vec3(
            .5f * (t.x - x) + x, 
            .5f * (t.y - y) + y,
            .5f * (t.z - z) + z);
    }
    public Vec3 subtract(Vec3 t){
        return new Vec3(x - t.x, y - t.y, z - t.z);
    }
    public Vec4 toVec4(){
        return new Vec4(x, y ,z, 0f);
    }
    @Override
    public String toString() {
        return x + ", " + y + ", " + z;
    }
    @Override
    public int hashCode() { //working hash function for vec3's, using precision to two decimal places
        //note: this function is useless for comparing vec3's with components > 1, as uses the first two digits to hash
        return 
            100000000 * (x < 0 ? 0 : 1) //x sign
            + 1000000 * Math.abs(((int)Math.floor(x * 100) / Math.max(1,((int)Math.log10(x * 100) - 1)))) //x
            + 100000 * (y < 0 ? 0 : 1) //y sign
            + 1000 * Math.abs(((int)Math.floor(y * 100) / Math.max(1,((int)Math.log10(y * 100) - 1)))) //y
            + 100 * (z < 0 ? 0 : 1) //z sign
            + Math.abs(((int)Math.floor(z * 100) / Math.max(1,((int)Math.log10(z * 100) - 1)))) //z
        ;
    }
    @Override
    public boolean equals(Object o){ 
        Vec3 t = (Vec3)o;
        if(x == t.x && y == t.y && z == t.z) return true;
        else return false;
    }
}