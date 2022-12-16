/*
 * Arcball type camera implementation using Quaternion matrix
 * Resource used in implementation:
 * Li, K. (2016). OpenGL Arcball Camera. CS171. Retrieved December 5, 2022, 
 * from http://courses.cms.caltech.edu/cs171/assignments/hw3/hw3-notes/notes-hw3.html#NotesSection4. 
 */
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
public class ArcballCam implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
    public final double ZFAR = 100;
    public final double ZNEAR = 1;
    private final double FOV = 80;
    private final double ROTSPEED = 1d;
    private final double MAX_DISTANCE = 15d * (float)EarthIcosphere.EARTH_RADIUS;
    private final double MIN_DISTANCE = EarthIcosphere.EARTH_RADIUS * 1.003d;
    private final boolean IS_ORTHOGRAPHIC = false;
    private final double ZOOMSPEED = 2000000f;

    private MapGraphics graphics;
    public Mat4x4 viewProjMatrix;
    private Mat4x4 projMatrix;
    private Vec4 rotation = new Vec4(1, 0, 0, 0);
    private Vec4 curRot = new Vec4(1, 0, 0, 0);
    private Vec4 prevRot = new Vec4(1, 0, 0, 0);
    private Vec3 mDownPos = new Vec3(0, 0, 0);
    public Vec3 position = new Vec3();
    private boolean mouseDown = false;
    private boolean transformUpdateNeeded = true;
    private double targDistance = MAX_DISTANCE / 1.5f;
    private double distance = MAX_DISTANCE;

    public ArcballCam(MapGraphics graphics){
        this.graphics = graphics;
        //SetTransform(new Vec4(1f, 0f, 0f, 0f), DISTANCE);
    }
    public void Update(){
        if(targDistance != distance){
            //zoom = zoom + ((targZoom - zoom) * 10d * graphics.deltaTime);
            distance = targDistance;
            transformUpdateNeeded = true;
        }
        if(transformUpdateNeeded) SetTransform(rotation, (float)distance);
    }
    private void SetTransform(Vec4 r, float distance){
        transformUpdateNeeded = false;
        //using identity that the view matrix = T*R inverse
        //which == R inverse * T inverse
        //where T = camera transform and R = camera rotation
        Mat4x4 viewMatrix = new Mat4x4(new float[][]{
            //translation matrix
            {1, 0, 0, 0},
            {0, 1, 0, 0},
            {0, 0, 1, -distance},
            {0, 0, 0, 1}}
        );
        //getting camera position from quaternion matrix
        position = new Vec3(
            distance*(2*r.y*r.w + 2*r.x*r.z),
            -1*distance*(1 - 2*r.y*r.y - 2*r.z*r.z),
            -1*distance*(2*r.z*r.w - 2*r.x*r.y)
        );
        r = r.QuaternionInverse();
        r.Normalize();
        //remark: we know [Quaternion]^-1 = [Quaternion^-1]
        viewMatrix = viewMatrix.matProduct(new Mat4x4(new float[][]{
            //inverted rotation quaternion matrix
            {1 - 2*r.z*r.z - 2*r.w*r.w, 2*r.y*r.z - 2*r.w*r.x,     2*r.y*r.w + 2*r.z*r.x,     0},
            {2*r.y*r.z + 2*r.w*r.x,     1 - 2*r.y*r.y - 2*r.w*r.w, 2*r.z*r.w - 2*r.y*r.x,     0},
            {2*r.y*r.w - 2*r.z*r.x,     2*r.z*r.w + 2*r.y*r.x,     1 - 2*r.y*r.y - 2*r.z*r.z, 0},
            {0,                         0,                         0,                         1}}
        ));
        viewMatrix = viewMatrix.matProduct(new Mat4x4(new float[][]{
            //get camera to desired +z up right-handed orientation
            {1f, 0, 0, 0},
            {0, 0, -1f, 0},
            {0, -1f, 0, 0},
            {0, 0, 0, 1f}})
        );
        //if desired to rotate center outside of origin, should translate here
        //creating projection matrix
        projMatrix = new Mat4x4(new float[4][4]);
        if(IS_ORTHOGRAPHIC){
            projMatrix = new Mat4x4(new float[][]{
                {200/(float)graphics.width, 0, 0, 0},
                {0, 200/(float)graphics.height, 0, 0},
                {0, 0, (float)(-2 /(ZFAR - ZNEAR)), (float)(-1 * (ZNEAR + ZFAR)/(ZFAR - ZNEAR))},
                {0, 0, 0, 1}}
                );
        }
        else{
            projMatrix = new Mat4x4(new float[][]{
                {(float)Math.atan(FOV / 2d) * (float)((double)graphics.height / (double)graphics.width), 0, 0, 0},
                {0, (float)Math.atan(FOV / 2d), 0, 0},
                {0, 0, (float)(-1 * (ZFAR + ZNEAR)/(ZFAR - ZNEAR)), (float)(-2 * (ZNEAR*ZFAR)/(ZFAR - ZNEAR))},
                {0, 0, -1, 0}}
            );
        }
        viewProjMatrix = projMatrix.matProduct(viewMatrix);
        projMatrix = projMatrix.matProduct(new Mat4x4(new float[][]{
            //distance adjustment used for mouse drag distance detection
            {1, 0, 0, 0},
            {0, 1, 0, 0},
            {0, 0, 1, -distance},
            {0, 0, 0, 1}}
        ));
    }
    private Vec3 pixToSpherePos(int x, int y){
        x = Math.min(graphics.width, Math.max(0, x));//clamp pix coords in screen bounds
        y = Math.min(graphics.height, Math.max(0, y));
        Vec4 std = projMatrix.v4product(new Vec3(1, 1, 0));
        float dx = graphics.getWidth() * EarthIcosphere.EARTH_RADIUS * (std.x / std.w);
        float dy = graphics.getHeight() * EarthIcosphere.EARTH_RADIUS * (std.y / std.w);
        Vec3 res = new Vec3(
            ((float)(x - graphics.getWidth() / 2f)) /  (dx * .5f),
            ((float)(y - graphics.getHeight() / 2f)) /  (dy * -.5f), 0f);
        if(res.Magnitude() > 1f){//outside of sphere. clip to nearest sphere pt
            res.Normalize();
            res.z = 0;
        }
        else res.z = (float)Math.sqrt(1 - res.x * res.x - res.y * res.y);
        if(res.z < .5f) res.z = .5f;
        return res;
    }
    private boolean mouseOverSphere(int x, int y){
        x = Math.min(graphics.width, Math.max(0, x));//clamp pix coords in screen bounds
        y = Math.min(graphics.height, Math.max(0, y));
        Vec4 std = projMatrix.v4product(new Vec3(1, 1, 0));
        float dx = graphics.getWidth() * EarthIcosphere.EARTH_RADIUS * (std.x / std.w);
        float dy = graphics.getHeight() * EarthIcosphere.EARTH_RADIUS * (std.y / std.w);
        Vec3 res = new Vec3(
            ((float)(x - graphics.getWidth() / 2f)) /  (dx * .5f),
            ((float)(y - graphics.getHeight() / 2f)) /  (dy * -.5f), 0f);
        if(res.Magnitude() > 1) return false; //outside of sphere
        return true;
    }

    //keyboard events
    public void keyTyped(KeyEvent e) {
    }
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_SPACE){
            System.out.println("Tris: " + graphics.triCt);
            System.out.println("FPS: " + graphics.fps);
        }
    }
    public void keyReleased(KeyEvent e) {
    }

    //mouse events
    public void mouseDragged(MouseEvent e) {
        if(!mouseDown) return;
        Vec3 mPos = pixToSpherePos(e.getX(), e.getY());
        Vec3 axis = mDownPos.cross(mPos);
        double x = ((MAX_DISTANCE - targDistance) * 100d / (MAX_DISTANCE - MIN_DISTANCE));
        double rSpeedFct = Math.min(1d, Math.pow(Math.E, -.3d*(x - 90d)));
        curRot = new Vec4(
            (1f + (float)mDownPos.dot(mPos)) / (float)(ROTSPEED * rSpeedFct), axis.x, -axis.y, 0f
        );
        curRot.Normalize();
        rotation = prevRot.QuaternionMultiply(curRot);
        transformUpdateNeeded = true;
    }
    public void mouseMoved(MouseEvent e) { 
    }
    public void mouseClicked(MouseEvent e) {        
    }
    public void mousePressed(MouseEvent e) {     
        if(!mouseOverSphere(e.getX(), e.getY())) return;
        mouseDown = true;
        mDownPos = pixToSpherePos(e.getX(), e.getY());
    }
    public void mouseReleased(MouseEvent e) {        
        if(!mouseDown) return;
        mouseDown = false;
        prevRot = prevRot.QuaternionMultiply(curRot);
        curRot = new Vec4(1, 0, 0, 0);
    }
    public void mouseEntered(MouseEvent e) {        
    }
    public void mouseExited(MouseEvent e) {        
    }

    //mouse wheel events
    public void mouseWheelMoved(MouseWheelEvent e) {
        double x = ((MAX_DISTANCE - targDistance) * 100d / (MAX_DISTANCE - MIN_DISTANCE));
        double rSpeedFct = Math.min(1d, Math.pow(Math.E, -.2d*(x - 86d)));
        double prevTargDistance = targDistance;
        targDistance += e.getPreciseWheelRotation() * ZOOMSPEED * rSpeedFct;
        targDistance = Math.min(MAX_DISTANCE, targDistance);
        if(targDistance < MIN_DISTANCE) targDistance = prevTargDistance - (prevTargDistance - MIN_DISTANCE) / 2d;
    }
}