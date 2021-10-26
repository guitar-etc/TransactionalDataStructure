package etc.transactional;

/**
 * Extends Error, hoping no application code will catch it.
 * @author pycs9
 *
 */
public class TXCancelledError extends Error {
	private static final long serialVersionUID = 1L;

	private TX tx;

	public TXCancelledError(TX tx) {
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
