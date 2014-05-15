package hex;

import com.amazonaws.services.cloudfront.model.InvalidArgumentException;
import water.*;
import water.api.DocGen;
import water.fvec.*;
import water.util.Log;
import water.util.RString;

import java.util.Random;

/**
 * Create a Frame from scratch
 * If randomize = true, then the frame is filled with Random values.
 *
 */
public class CreateFrame extends Request2 {
  static final int API_WEAVER=1; // This file has auto-gen'd doc & json fields
  static public DocGen.FieldDoc[] DOC_FIELDS; // Initialized from Auto-Gen code.

  @API(help = "Name (Key) of frame to be created", required = true, filter = Default.class, json=true)
  public String key;

  @API(help = "Number of rows", required = true, filter = Default.class, json=true)
  public long rows = 1000;

  @API(help = "Number of columns", required = true, filter = Default.class, json=true)
  public int cols = 10;

  @API(help = "Random number seed", filter = Default.class, json=true)
  public long seed = new Random().nextLong();

  @API(help = "Whether frame should be randomized", filter = Default.class, json=true)
  public boolean randomize = true;

  @API(help = "Constant value (for randomize=false)", filter = Default.class, json=true)
  public long value = 0;

  @API(help = "Fraction of categorical columns (for randomize=true)", filter = Default.class, json=true)
  public double categorical_fraction = 0.2;

  @API(help = "Factor levels for categorical variables", filter = Default.class, json=true)
  public int factors = 100;

  @API(help = "Fraction of integer columns (for randomize=true)", filter = Default.class, json=true)
  public double integer_fraction = 0.2;

  @API(help = "Range for integer variables (-range ... range)", filter = Default.class, json=true)
  public int integer_range = 100;

  @API(help = "Fraction of missing values", filter = Default.class, json=true)
  public double missing_fraction = 0.01;

  @Override public Response serve() {
    try {
      if (integer_fraction + categorical_fraction > 1)
        throw new InvalidArgumentException("Integer and categorical fractions must add up to <= 1.");

      if (cols == 0 || rows == 0)
        throw new InvalidArgumentException("Must have non-zero number of rows and columns.");

      if (!randomize) {
        if (integer_fraction != 0 || categorical_fraction != 0)
          throw new InvalidArgumentException("Cannot have integer or categorical fractions > 0 unless randomize=true.");
      } else {
        if (value != 0)
          throw new InvalidArgumentException("Cannot set data to a constant value if randomize=true.");
      }

      final FrameCreator fct = new FrameCreator(this);
      H2O.submitTask(fct);
      fct.join();

      Log.info("Created frame '" + key.toString() + "'.");
      return Response.done(this);
    } catch( Throwable t ) {
      return Response.error(t);
    }
  }

  @Override public boolean toHTML( StringBuilder sb ) {
    Frame fr = UKV.get(Key.make(key));
    if (fr==null) {
      return false;
    }
    RString aft = new RString("<a href='Inspect2.html?src_key=%$key'>%key</a>");
    aft.replace("key", key);
    DocGen.HTML.section(sb, "Frame creation done. Frame '" + aft.toString()
            + "' now has " + fr.numRows() + " rows and " + fr.numCols() + " columns (" + fr.anyVec().nChunks()
            + " chunks).");
    return true;
  }

}
