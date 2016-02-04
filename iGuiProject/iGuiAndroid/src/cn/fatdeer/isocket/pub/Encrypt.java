package cn.fatdeer.isocket.pub;

public class Encrypt {

	private static long BYTE_1 = 0xFFL;
	private static long BYTE_2 = 0xFF00L;
	private static long BYTE_3 = 0xFF0000L;
	private static long BYTE_4 = 0xFF000000L;
	boolean DEBUG_FLAG = false;

	// v0 64bits v1 64bits k[4] 128bits
	// long 32 bits
	public byte[] tea_encrypt(long v0, long v1, long k[]) {
		long y = getUnsignedLong(v0), z = getUnsignedLong(v1);
		long sum = 0;
		long delta = getUnsignedLong(0x9e3779b9);
		long a = getUnsignedLong(k[0]), b = getUnsignedLong(k[1])
				, c = getUnsignedLong(k[2]), d = getUnsignedLong(k[3]);
		long tmp1 = 0, tmp2 = 0, tmp3 = 0, tmp4 = 0, tmp5 = 0, tmp6 = 0;

		for (int i = 0; i < 32; i++) {
			sum += delta;
			tmp1 = getUnsignedLong(z << 4 );
			tmp2 = getUnsignedLong(tmp1 + a);
			tmp3 = getUnsignedLong((z + sum));
			tmp4 = getUnsignedLong(z >> 5);
			tmp5 = getUnsignedLong(tmp4 + b);
			y += ((tmp2) ^ (tmp3) ^ (tmp5));
			y = getUnsignedLong(y);
			tmp1 = getUnsignedLong(y << 4);
			tmp2 = getUnsignedLong(tmp1 + c);
			tmp3 = getUnsignedLong(y + sum);
			tmp4 = getUnsignedLong(y >> 5);
			tmp5 = getUnsignedLong(tmp4 + d);
			tmp6 = getUnsignedLong((tmp2) ^ (tmp3) ^ (tmp5));
			z += tmp6;
			z = getUnsignedLong(z);
		}
		v0 = y;
		v1 = z;
		byte[] b0 = long_to_bytes(v0, 4);
		byte[] b1 = long_to_bytes(v1, 4);
		return new byte[] { 
			b0[0],b0[1],b0[2],b0[3], 
			b1[0],b1[1],b1[2],b1[3] 
		};
	}

	public long getUnsignedLong(long data) { // ��int���ת��Ϊ0~4294967295
												// (0xFFFFFFFF��DWORD)��
		return data & 0x0FFFFFFFFL;
	}

	/**
	 * �� long ���͵� n תΪ byte ���飬��� len Ϊ 4����ֻ���ص�32λ��4��byte
	 * 
	 * @param n
	 *            ��Ҫת����long
	 * @param len
	 *            ��Ϊ4����ֻ���ص�32λ��4��byte�����򷵻�8��byte
	 * @return ת����byte����
	 */
	private static byte[] long_to_bytes(long n, int len) {
		byte d = (byte) ((n & BYTE_4) >> 24);
		byte c = (byte) ((n & BYTE_3) >> 16);
		byte b = (byte) ((n & BYTE_2) >> 8);
		byte a = (byte) (n & BYTE_1);
		return new byte[] { a, b, c, d };
	}

	/**
	 * ��4��byteתΪ Unsigned Integer 32���� long ��ʽ����
	 * 
	 * @param bs
	 *            ��Ҫת�����ֽ�
	 * @return ���� long����32λΪ0����32λ��ΪUnsigned Integer
	 */
	private static long bytes_to_long(byte[] bs) {
		return ((bs[3] << 24) & BYTE_4) + ((bs[2] << 16) & BYTE_3)
				+ ((bs[1] << 8) & BYTE_2) + (bs[0] & BYTE_1);
	}

	/*********************************************************************
	 * tea���� ����:v:Ҫ���ܵ����,����Ϊ8�ֽ� k:�����õ�key,����Ϊ16�ֽ�
	 **********************************************************************/

	byte[] tea_decrypt(long v0, long v1, long k[]) {

		long y = getUnsignedLong(v0), z = getUnsignedLong(v1);
		long sum = getUnsignedLong(0xC6EF3720);
		long delta = getUnsignedLong(0x9e3779b9);
		long a = getUnsignedLong(k[0]), b = getUnsignedLong(k[1]), 
			c = getUnsignedLong(k[2]), d = getUnsignedLong(k[3]);
		long tmp1 = 0, tmp2 = 0, tmp3 = 0, tmp4 = 0, tmp5 = 0, tmp6 = 0;

		for (int i = 0; i < 32; i++) {
			tmp1 = getUnsignedLong(y << 4);
			tmp2 = getUnsignedLong(tmp1 + c);
			tmp3 = getUnsignedLong(y + sum);
			tmp4 = getUnsignedLong(y >> 5);
			tmp5 = getUnsignedLong(tmp4 + d);
			tmp6 = getUnsignedLong((tmp2) ^ (tmp3) ^ (tmp5));
			z -= tmp6;
			z = getUnsignedLong(z);
			tmp1 = getUnsignedLong(z << 4);
			tmp2 = getUnsignedLong(tmp1 + a);
			tmp3 = getUnsignedLong(z + sum);
			tmp4 = getUnsignedLong(z >> 5);
			tmp5 = getUnsignedLong(tmp4 + b);
			tmp6 = getUnsignedLong((tmp2) ^ (tmp3) ^ (tmp5));
			y -= tmp6;
			y = getUnsignedLong(y);
//			if (DEBUG_FLAG) {
//				System.out.print("y+((z<<4)+a)^(z+sum)^((z>>5)+b) =");
//				System.out.println(y);
//			}
			sum -= delta;
			sum = getUnsignedLong(sum);

		}
		v0 = y;
		v1 = z;
		byte[] b0 = long_to_bytes(v0, 4);
		byte[] b1 = long_to_bytes(v1, 4);
		return new byte[] { b0[0], b0[1], b0[2], b0[3], b1[0], b1[1], b1[2],
				b1[3] };

	}

	// ����
	public byte[] encrypt(byte rowcontent[], int size_src, byte key[]) {// timesΪ��������

		int loop=rowcontent.length;
		while(loop%8!=0) {
//at 20150821			if(Const.isDEBUG) 
//			if(Server.instance().cb_debug.isSelected())	
//				Server.instance().contentArea.append("loop=" + loop + "\r\n");
			loop++;

		}
		byte[] content=new byte[loop];
		for (int i=0;i<loop;i++) {
			if(i<rowcontent.length) content[i]=rowcontent[i];
			else content[i]=0;
		}
			
		long[] keys = new long[4];
		for (int i = 0; i < 4; i++) {
			byte[] xkey = new byte[4];
			for (int j = 0; j < 4; j++) {
				xkey[j] = key[4 * i + j];
			}
			keys[i] = bytes_to_long(xkey);

		}
		byte[] rtn = new byte[loop];
		int num = loop / 8;
		for (int i = 0; i < num; i++) {
			byte[] v0 = new byte[4];
			byte[] v1 = new byte[4];
			for (int j = 0; j < 4; j++) {
				v0[j] = content[i*8+j];
				v1[j] = content[i*8+j + 4];
			}
			byte[] teas = tea_encrypt(bytes_to_long(v0), bytes_to_long(v1),
					keys);
			for (int k = 0; k < 8; k++) {
				rtn[i * 8 + k] = teas[k];
			}
		}
		return rtn;
	}

	// ����
	public byte[] decrypt(byte[] content, int size_src, byte[] key) {

		int loop=content.length;
		byte[] encryptContent=new byte[loop];
		for (int i=0;i<loop;i++) {
			if(i<content.length) encryptContent[i]=content[i];
			else encryptContent[i]=0;
		}
		
		long[] keys = new long[4];
		for (int i = 0; i < 4; i++) {
			byte[] xkey = new byte[4];
			for (int j = 0; j < 4; j++) {
				xkey[j] = key[4 * i + j];
			}
			keys[i] = bytes_to_long(xkey);

		}

		byte[] rtn = new byte[loop];
		int num = loop / 8;
		for (int i = 0; i < num; i++) {
			byte[] v0 = new byte[4];
			byte[] v1 = new byte[4];
			for (int j = 0; j < 4; j++) {
				v0[j] = encryptContent[8*i+j];
				v1[j] = encryptContent[8*i+j + 4];
			}
			byte[] teas = tea_decrypt(bytes_to_long(v0), bytes_to_long(v1),
					keys);
			for (int k = 0; k < 8; k++) {
				rtn[i * 8 + k] = teas[k];
			}
		}
		return rtn;

	}
}
