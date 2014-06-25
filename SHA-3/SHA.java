import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;


public class SHA {
static int w,l,n;
private static BigInteger BIT_64 = new BigInteger("18446744073709551615");
static BigInteger[] RC;
static int r[][];

	public static void main(String args[]){
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		String inputVal=null;
		int ch=0;
		while(true){
		System.out.println("Menu:");
		System.out.println("1.SHA3");
		System.out.println("2.Exit");
		System.out.println("\nEnter your choice");
		try {
			ch=Integer.parseInt(br.readLine());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		switch(ch){
		case 1:
		System.out.println("Enter Input String:");//The quick brown fox jumps over the lazy dog
		try {
			inputVal=br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte b[]=inputVal.getBytes();
		String inVal=bytesToHex(b);
		//Send Bit rate=1088, Capacity=512,Word Size=64
		initialize(1600);
		initiaizeRC();
		initRoundConst();
		String hashval=calculateSHAHashValue(inVal,1088,512,32);
		System.out.println("Hash Value is::"+hashval);
		break;
		case 2:
			System.out.println("Thank You");
			System.exit(1);
		}
		
		}
	}
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	private static void initRoundConst(){
		r=new int[][]{
				{0, 36, 3, 41, 18},
				{1, 44, 10, 45, 2},
				{62, 6, 43, 15, 61},
				{28, 55, 25, 21, 56},
				{27, 20, 39, 8, 14}
			};
	}
	private static void initiaizeRC(){
		RC=new BigInteger[]{
				new BigInteger("0000000000000001", 16),
				new BigInteger("0000000000008082", 16),
				new BigInteger("800000000000808A", 16),
				new BigInteger("8000000080008000", 16),

				new BigInteger("000000000000808B", 16),
				new BigInteger("0000000080000001", 16),
				new BigInteger("8000000080008081", 16),
				new BigInteger("8000000000008009", 16),
				new BigInteger("000000000000008A", 16),

				new BigInteger("0000000000000088", 16),
				new BigInteger("0000000080008009", 16),
				new BigInteger("000000008000000A", 16),
				new BigInteger("000000008000808B", 16),
				new BigInteger("800000000000008B", 16),

				new BigInteger("8000000000008089", 16),
				new BigInteger("8000000000008003", 16),
				new BigInteger("8000000000008002", 16),
				new BigInteger("8000000000000080", 16),
				new BigInteger("000000000000800A", 16),

				new BigInteger("800000008000000A", 16),
				new BigInteger("8000000080008081", 16),
				new BigInteger("8000000000008080", 16),
				new BigInteger("0000000080000001", 16),
				new BigInteger("8000000080008008", 16)
			};
	}
	private static void initialize(int b){
		w= b/25;
		l=(int)(Math.log(w)/Math.log(2));
		n= 12 + 2*l;
	}
	
	private static String calculateSHAHashValue(String hexString,int r,int c,int d){
		
		//Step 1:Initialization
		BigInteger S[][]=new BigInteger[5][5];
		for(int i=0;i<5;i++)
			for(int j=0;j<5;j++)
				S[i][j]=new BigInteger("0",16);
		
		//Step 2:padMessage
		BigInteger P[][]=padMessage(hexString,r);
		
		//Step 3:Absorbing Phase
		
		BigInteger Pi[]=new BigInteger[25];
		for(int k=0;k<P.length;k++){
			Pi=P[k];
		for(int i=0;i<5;i++)
			for(int j=0;j<5;j++)
				if((i+5*j)<(r/w))
					S[i][j]=S[i][j].xor(Pi[i+5*j]);
		}
		doKeccakRounds(S);
		
		//Step 4: Squeezing Phase
		String z="";
		do{
			for(int i=0;i<5;i++)
				for(int j=0;j<5;j++)
						if((5*i+j) <(r/w))
							z= z + hexReversion(S[j][i]).substring(0,16);
			doKeccakRounds(S);
		}while(z.length()<d*2);
		
		return z.substring(0,d*2);
	}
	
	private static void doKeccakRounds(BigInteger[][] S){
		for(int i=0;i<n;i++)
			S=roundB(S,RC[i]);
	}
	
	private static BigInteger[][] roundB(BigInteger[][] S,BigInteger RC){
		BigInteger B[][]=new BigInteger[5][5];
		
		//Theta Step
		S=theta(S);
		
		//Rho and Pi Steps
		B=RhoPi(S);
		
		//Gama
		S=gama(B,S);
		
		//Xi
		S[0][0]=S[0][0].xor(RC);
		
		return S;
	}
	
	private static BigInteger[][] gama(BigInteger[][] B,BigInteger [][]S){
		
		for(int i=0;i<5;i++)
			for(int j=0;j<5;j++)
				S[i][j]=B[i][j].xor(B[(i+1)%5][j].not().and(B[(i+2)%5][j]));
		return S;
	}
	private static BigInteger[][] RhoPi(BigInteger[][] S){
		BigInteger B[][]=new BigInteger[5][5];
		for(int i=0;i<5;i++)
			for(int j=0;j<5;j++)
				B[j][(2*i+3*j)%5]=rotate(S[i][j],r[i][j]);
				
		return B;
	}
	
	private static BigInteger[][] theta(BigInteger[][] S){
		BigInteger C[]=new BigInteger[5];
		BigInteger D[]=new BigInteger[5];
		
		for(int i=0;i<5;i++)
		C[i]=S[i][0].xor(S[i][1]).xor(S[i][2]).xor(S[i][3]).xor(S[i][4]);
		
		for(int i=0;i<5;i++)
		D[i]=C[(i+4)%5].xor(rotate(C[(i+1)%5],1));
		
		for(int i=0;i<5;i++)
			for(int j=0;j<5;j++)
				S[i][j]=S[i][j].xor(D[i]);
		return S;
	}
	
	private static BigInteger rotate(BigInteger x,int n){
		n=n%w;
		
		BigInteger retV = x.shiftLeft(n);
		BigInteger tmp = x.shiftLeft(n);

		if (retV.compareTo(BIT_64) > 0) {
		for (int i = 64; i < 64 + n; i++)
		tmp = tmp.clearBit(i);
		tmp = tmp.setBit(64 + n);
		retV = tmp.and(retV);
		}
		
		BigInteger leftShift=retV;
		BigInteger rightShift=x.shiftRight(w-n);
		
		return leftShift.or(rightShift);
	}
	
	private static BigInteger[][] padMessage(String hexmessage,int r){
		
		hexmessage +="01";
		
		while(((hexmessage.length()/2)*8 % r) != (r-8))
		hexmessage +="00";
			
		hexmessage +="80";
		
		
		BigInteger arrayM[][]=new BigInteger[1][25];
		for(int i=0;i<25;i++)
		arrayM[0][i]=new BigInteger("0",16);
		
		int i=0,j=0,count=0;
		
		for(int n=0;n<hexmessage.length();n++){
		count++;
		if((count*4)%w==0){
			String originalString=hexmessage.substring((count-w/4),(w/4)+(count-w/4));
			arrayM[i][j]=new BigInteger(originalString,16);
			String revertString=hexReversion(arrayM[i][j]);
			revertString=addzero(revertString,originalString.length());	
			arrayM[i][j]=new BigInteger(revertString,16);
			j++;
		}	
		}
		return arrayM;
	}
	
	private static String hexReversion(BigInteger arr){
		byte b[]=arr.toByteArray();
		b=byteReversion(b);
		
		String inVal=bytesToHex(b);
		
		return inVal;
	}
	
	private static byte[] byteReversion(byte b[]){
		int i,j;
		byte tmp;
		i=0;
		j=b.length -1 ;
		while(i<j){
			tmp=b[i];
			b[i]=b[j];
			b[j]=tmp;
			i++;
			j--;
		}
		return b;
	}
	
	private static String addzero(String revertString,int length){
		while(revertString.length()<length){
		revertString=revertString+"0";	
		}
		return revertString;
	}

}
