package etc.transactional;

public class TXCancelledException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private TX tx;

	public TXCancelledException(TX tx) {
		this.tx = tx;
	}

	public TX getTx() {
		return tx;
	}

	@Override
	public String toString() {
		return "TXCancelledException [tx=" + tx + "]";
	}
}
