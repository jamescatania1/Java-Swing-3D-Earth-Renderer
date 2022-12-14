/*
 * jcatani4@u.rochester.edu
 * 4x4 float matrix. I managed to not have to explicitly 
 * invert any matrix in this project, so it is rather simple
 */
public class Mat4x4{
    public float[][] vals = new float[4][4];
    public Mat4x4(float[][] vals){
        this.vals = vals;
    }
    public Vec4 v4product(Vec4 vec){
        return new Vec4(
            vals[0][0] * vec.x + vals[0][1] * vec.y + vals[0][2] * vec.z + vals[0][3] * vec.w,
            vals[1][0] * vec.x + vals[1][1] * vec.y + vals[1][2] * vec.z + vals[1][3] * vec.w,
            vals[2][0] * vec.x + vals[2][1] * vec.y + vals[2][2] * vec.z + vals[2][3] * vec.w,
            vals[3][0] * vec.x + vals[3][1] * vec.y + vals[3][2] * vec.z + vals[3][3] * vec.w
        );
    }
    public Vec4 v4product(Vec3 vec){
        return new Vec4(
            vals[0][0] * vec.x + vals[0][1] * vec.y + vals[0][2] * vec.z + vals[0][3],
            vals[1][0] * vec.x + vals[1][1] * vec.y + vals[1][2] * vec.z + vals[1][3],
            vals[2][0] * vec.x + vals[2][1] * vec.y + vals[2][2] * vec.z + vals[2][3],
            vals[3][0] * vec.x + vals[3][1] * vec.y + vals[3][2] * vec.z + vals[3][3]
        );
    }
    public Mat4x4 matProduct(Mat4x4 rMat){
        Mat4x4 res = new Mat4x4(new float[4][4]);
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                for(int k = 0; k < 4; k++){ //dot ith row with jth col of rMat
                    res.vals[i][j] += vals[i][k] * rMat.vals[k][j];
                }
            }
        }
        return res;
    }
    public Mat4x4 transpose(){
        Mat4x4 res = new Mat4x4(new float[4][4]);
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                res.vals[i][j] = vals[j][i];
            }
        }
        return res;
    }
    @Override
    public String toString() {
        String res = "";
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                res += vals[i][j] + "   ";
            }
            res += "\n";
        }
        return res;
    }
}