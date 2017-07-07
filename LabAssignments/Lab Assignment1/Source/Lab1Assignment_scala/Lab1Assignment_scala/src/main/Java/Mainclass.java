/**
 * Created by rohithkumar on 6/12/17.
 */
public class Mainclass {
    public static void main(String args[]){
        float result;
        float a[]={1,2,3};
        float b[]= {4,5,6};
        result= DenseVectorSupportMethods.smallDotProduct_Float(a,b,3);
        System.out.print("product of 3*3 matrix is: "+result);
    }
}
