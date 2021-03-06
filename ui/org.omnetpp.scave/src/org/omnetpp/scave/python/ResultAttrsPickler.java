package org.omnetpp.scave.python;

import java.io.IOException;
import java.io.OutputStream;

import org.omnetpp.common.Debug;
import org.omnetpp.scave.engine.IDList;
import org.omnetpp.scave.engine.ResultFileManager;
import org.omnetpp.scave.engine.ResultItem;
import org.omnetpp.scave.engine.StringMap;
import org.omnetpp.scave.engine.StringVector;

import net.razorvine.pickle.IObjectPickler;
import net.razorvine.pickle.Opcodes;
import net.razorvine.pickle.PickleException;
import net.razorvine.pickle.Pickler;

public class ResultAttrsPickler implements IObjectPickler {

    static void pickleResultAttr(ResultFileManager resultManager, long ID, Pickler pickler, OutputStream out)
            throws PickleException, IOException {
        ResultItem result = resultManager.getItem(ID);

        StringMap attrs = result.getAttributes();
        StringVector attrKeys = attrs.keys();
        for (int i = 0; i < attrKeys.size(); ++i) {
            String key = attrKeys.get(i);

            out.write(Opcodes.MARK);
            {
                pickler.save(result.getRun().getRunName());
                pickler.save(result.getModuleName());
                pickler.save(result.getName());
                pickler.save(key);
                pickler.save(attrs.get(key));
            }
            out.write(Opcodes.TUPLE);
        }
    }

    String filter;

    public ResultAttrsPickler(String filter) {
        this.filter = filter;
    }

    @Override
    public void pickle(Object obj, OutputStream out, Pickler pickler) throws PickleException, IOException {
        ResultFileManager resultManager = (ResultFileManager)obj;

        out.write(Opcodes.MARK);
        {
            IDList items = resultManager.getAllItems(false, false);
            items = resultManager.filterIDList(items, filter);

            Debug.println("pickling attrs of " + items.size() + " items");

            for (int i = 0; i < items.size(); ++i)
                pickleResultAttr(resultManager, items.get(i), pickler, out);
        }
        out.write(Opcodes.LIST);
    }
}
