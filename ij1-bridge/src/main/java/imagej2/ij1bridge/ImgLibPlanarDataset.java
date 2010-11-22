package imagej2.ij1bridge;

import ij.PlanarDataset;
import ij.process.ImageProcessor;
import imagej2.Utils;
import imagej2.ij1bridge.process.ImgLibProcessor;
import imagej2.imglib.process.ImageUtils;
import imagej2.process.Index;
import mpicbg.imglib.container.basictypecontainer.PlanarAccess;
import mpicbg.imglib.container.basictypecontainer.array.ArrayDataAccess;
import mpicbg.imglib.image.Image;

//TODO
// thinking about how to order the planes so that IJ is going to be able to do something understandable to the data
//   ImageJ has a default ordering. so populate our stack that way.
//   throw an exception right now if dimensionality is > 5 or if an axis is not X/Y/Z/C/or/T

public class ImgLibPlanarDataset implements PlanarDataset
{
	private Image<?> image;
	
	public ImgLibPlanarDataset(Image<?> image)
	{
		this.image = image;
	}
	
	@Override
	public int getPlaneCount()
	{
		int height = getPlaneHeight();
		
		int width = getPlaneWidth();
		
		long totalSamples = ImageUtils.getTotalSamples(this.image);
		
		long planeSize = (long)width * height;
		
		return (int)(totalSamples / planeSize);
	}
	
	@Override
	public Object getPrimitiveArray(int planeNumber)
	{
		final PlanarAccess<ArrayDataAccess<?>> planarAccess = ImageUtils.getPlanarAccess(this.image);
		
		if (planarAccess == null)
			throw new UnsupportedOperationException("not yet supporting nonprimitive access to imglib image planes");
		
		int[] lengths = Utils.getDims3AndGreater(this.image.getDimensions());
		
		int[] planePos = Index.getPlanePosition(this.image.getDimensions(), planeNumber);
		
		final int no = Index.positionToRaster(lengths, planePos);
		
		return planarAccess.getPlane(no).getCurrentStorageArray();
	}
	
	@Override
	public void setPrimitiveArray(int planeNumber, Object planeDataReference)
	{
		final PlanarAccess<ArrayDataAccess<?>> planarAccess = ImageUtils.getPlanarAccess(this.image);
		
		if (planarAccess == null)
			throw new UnsupportedOperationException("not yet supporting nonprimitive access to imglib image planes");
		
		int[] lengths = Utils.getDims3AndGreater(this.image.getDimensions());
		
		int[] planePos = Index.getPlanePosition(this.image.getDimensions(), planeNumber);
		
		final int no = Index.positionToRaster(lengths, planePos);
		
		planarAccess.setPlane(no, ImageUtils.makeArray(planeDataReference));
	}
	
	@Override
	public String getPlaneLabel(int planeNumber)
	{
		throw new UnsupportedOperationException("unimplemented");
	}

	@Override
	public void setPlaneLabel(int planeNumber, String label)
	{
		throw new UnsupportedOperationException("unimplemented");
	}

	@Override
	public int getPlaneWidth()
	{
		return ImageUtils.getWidth(this.image);
	}

	@Override
	public int getPlaneHeight()
	{
		return ImageUtils.getHeight(this.image);
	}

	@Override
	public void insertPlaneAt(int planeNumber, Object plane)
	{
		// TODO - make sure to default plane label to something
		throw new UnsupportedOperationException("unimplemented");
	}

	@Override
	public void deletePlaneAt(int planeNumber)
	{
		throw new UnsupportedOperationException("unimplemented");
	}

	@Override
	@SuppressWarnings({"unchecked","rawtypes"})
	public ImageProcessor getProcessor(int planeNumber)
	{
		// TODO - may need to specify type here. Try to query underlying image and test and then hardcode types if needed.
		return new ImgLibProcessor(this.image, planeNumber);
	}
}
