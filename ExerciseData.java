import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nishkarsh_shastri on 02-Oct-15.
 */
public class ExerciseData {
    Map<String,ArrayList<Double>> Acc;
    Map<String,ArrayList<Double>> Gyr;
    Map<String,ArrayList<Double>> Ori;
    ArrayList<String> l;



    ExerciseData()
    {
        Acc = new HashMap<String, ArrayList<Double>>();
        Ori = new HashMap<String, ArrayList<Double>>();
        Gyr = new HashMap<String, ArrayList<Double>>();
        l = new ArrayList<String>();
        l.add("x");
        l.add("y");
        l.add("z");
        for(int i=0;i<l.size();i++)
        {
            ArrayList<Double> list1 = new ArrayList<Double>();
            ArrayList<Double> list2 = new ArrayList<Double>();
            ArrayList<Double> list3 = new ArrayList<Double>();
            Acc.put(l.get(i), list1);
            Gyr.put(l.get(i), list2);
            Ori.put(l.get(i), list3);
        }
    }

    public void append_data(String data)
    {
        String[] values = data.split(" ");
        Acc.get("x").add(Double.parseDouble(values[0]));
        Acc.get("y").add(Double.parseDouble(values[1]));
        Acc.get("z").add(Double.parseDouble(values[2]));
        Ori.get("x").add(Double.parseDouble(values[3]));
        Ori.get("x").add(Double.parseDouble(values[4]));
        Ori.get("x").add(Double.parseDouble(values[5]));
        Gyr.get("x").add(Double.parseDouble(values[6]));
        Gyr.get("x").add(Double.parseDouble(values[7]));
        Gyr.get("x").add(Double.parseDouble(values[8]));
    }

    public void reduce_to(int value)
    {
        int interval = (Acc.get("x").size()/value);
        int i = 0;
        int cur = 0;

        Map<String,ArrayList<Double>> temp = new HashMap<String, ArrayList<Double>>();

        for(int j=0;j<l.size();j++)
        {
            temp.put(l.get(j),new ArrayList<Double>());
        }

        while(i<value) {
            for (int j = 0; j < l.size(); j++) {
                Double curVal = Acc.get(l.get(j)).get(cur);
                temp.get(l.get(j)).add(curVal);
            }
            cur = cur + interval;
            i = i+1;
        }

        for (int j = 0; j < l.size(); j++){
            Acc.put(l.get(j), temp.get(l.get(j)));
        }

        i = 0;
        cur = 0;
        for(int j=0;j<l.size();j++)
        {
            temp.put(l.get(j),new ArrayList<Double>());
        }

        while(i<value) {
            for (int j = 0; j < l.size(); j++) {
                Double curVal = Gyr.get(l.get(j)).get(cur);
                temp.get(l.get(j)).add(curVal);
            }
            cur = cur + interval;
            i = i+1;
        }

        for (int j = 0; j < l.size(); j++){
            Gyr.put(l.get(j), temp.get(l.get(j)));
        }


        i = 0;
        cur = 0;
        for(int j=0;j<l.size();j++)
        {
            temp.put(l.get(j),new ArrayList<Double>());
        }

        while(i<value) {
            for (int j = 0; j < l.size(); j++) {
                Double curVal = Ori.get(l.get(j)).get(cur);
                temp.get(l.get(j)).add(curVal);
            }
            cur = cur + interval;
            i = i+1;
        }

        for (int j = 0; j < l.size(); j++) {
            Ori.put(l.get(j), temp.get(l.get(j)));
        }
    }

    public double k_filter(double previous,double current,double alpha)
    {
        return current*alpha + (previous*(1.0-alpha));
    }

    public void  filter_data()
    {
        for(int j=0;j<l.size();j++)
        {
            for(int i=1;i<Acc.get(l.get(j)).size();i++)
            {
                Acc.get(l.get(j)).set(i,k_filter(Acc.get(l.get(j)).get(i-1),Acc.get(l.get(j)).get(i),0.1));
                Gyr.get(l.get(j)).set(i,k_filter(Acc.get(l.get(j)).get(i-1),Acc.get(l.get(j)).get(i),0.1));
                Ori.get(l.get(j)).set(i,k_filter(Acc.get(l.get(j)).get(i-1),Acc.get(l.get(j)).get(i),0.1));
            }
        }
    }

    public void make_continuous()
    {
        for(int j=0;j<l.size();j++)
        {
            for(int i=1;i<Ori.get(l.get(j)).size();i++)
            {
                if( Math.abs(Ori.get(l.get(j)).get(i)-Ori.get(l.get(j)).get(i-1)) > 0.1)
                    Ori.get(l.get(j)).set(i,Ori.get(l.get(j)).get(i-1));
            }
        }

    }


    private double calculateAverage(ArrayList <Double> marks) {
        if (marks == null || marks.isEmpty()) {
            return 0;
        }

        double sum = 0;
        for (Double mark : marks) {
            sum += mark;
        }

        return sum / marks.size();
    }

    public void normalize()
    {
        Double diff_max = 0.0;
        Double diff;
        for(int j=0;j<l.size();j++)
        {
            diff = Collections.max(Acc.get(l.get(j))) - Collections.min(Acc.get(l.get(j)));
            if(diff_max<diff)
                diff_max = diff;
        }

        for(int j=0;j<l.size();j++)
        {
            Double mean = calculateAverage(Acc.get(l.get(j)));
            for(int i=1;i<Acc.get(l.get(j)).size();i++)
            {
                Double newVal = (Acc.get(l.get(j)).get(i)-mean)/diff_max;
                Acc.get(l.get(j)).set(i,newVal);
            }
        }

        diff_max = 0.0;

        for(int j=0;j<l.size();j++)
        {
            diff = Collections.max(Ori.get(l.get(j))) - Collections.min(Ori.get(l.get(j)));
            if(diff_max<diff)
                diff_max = diff;
        }

        for(int j=0;j<l.size();j++)
        {
            Double mean = calculateAverage(Ori.get(l.get(j)));
            for(int i=1;i<Ori.get(l.get(j)).size();i++)
            {
                Double newVal = (Ori.get(l.get(j)).get(i)-mean)/diff_max;
                Ori.get(l.get(j)).set(i,newVal);
            }
        }

        diff_max = 0.0;

        for(int j=0;j<l.size();j++)
        {
            diff = Collections.max(Gyr.get(l.get(j))) - Collections.min(Gyr.get(l.get(j)));
            if(diff_max<diff)
                diff_max = diff;
        }

        for(int j=0;j<l.size();j++)
        {
            Double mean = calculateAverage(Gyr.get(l.get(j)));
            if(diff_max==0.0)
            {
                diff_max=1.0;
            }

            for(int i=1;i<Gyr.get(l.get(j)).size();i++)
            {
                Double newVal = (Gyr.get(l.get(j)).get(i)-mean)/diff_max;
                Gyr.get(l.get(j)).set(i,newVal);
            }
        }
    }

    public void save_data(String file_name)
    {
        try {
            PrintWriter writer = new PrintWriter(file_name+".txt","UTF-8");
            for(int i=0;i<Acc.get("x").size();i++)
            {
                String acc = new String();
                String gyr = new String();
                String ori = new String();
                for(int j=0;j<l.size();j++)
                {
                    acc = acc+" " + String.valueOf(Acc.get(l.get(j)).get(i));
                    ori = ori+" " + String.valueOf(Acc.get(l.get(j)).get(i));
                    gyr = gyr+" " + String.valueOf(Acc.get(l.get(j)).get(i));
                }
                String put = acc + " " + ori + " " + gyr;
                writer.println(put);
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}
