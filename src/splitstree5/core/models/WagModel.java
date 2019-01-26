/*
 *  Copyright (C) 2019 Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 */
package splitstree5.core.models;

/**
 * @author bryant
 * <p/>
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class WagModel extends ProteinModel {


    public WagModel() {

        /*
         * Protein evolution matrix based on
         * Whelan, S. and N. Goldman.  In press.  A general empirical model of
         *	protein evolution derived from multiple protein families using
         *	a maximum likelihood approach.  Molecular Biology and Evolution.
         *
         * The matrix was recovered from the .dat files circulated with PAML 3.13d
         */

        /* For efficiency, we precompute the eigenvalue decomposition
         *     V'DV = Pi^(1/2) Q Pi(-1/2)
         * We used matlab for this.
         */

        this.evals = new double[]{
                -1.77917,
                -1.70753,
                -1.66555,
                -1.54831,
                -1.40703,
                -1.38904,
                -1.27621,
                -1.21884,
                -1.07122,
                -1.00502,
                -0.934284,
                -0.855026,
                -0.790741,
                -0.629673,
                -0.554998,
                -0.550378,
                -0.488933,
                -0.455159,
                -0.351227,
                -3.86426e-16};

        this.evecs = new double[][]{{0.184479, -0.258079, -0.0450254, -0.108135, 0.242152, 0.0996303, 0.525314, 0.485774, 0.0448099, 0.219648, 0.172798, -0.317328, -0.105666, -0.0726412, 0.00190063, -0.0577658, 0.0331176, 0.0472554, -0.120824, -0.294326},
                {0.104282, 0.0372348, -0.00957915, 0.0428248, -0.191153, -0.270057, 0.467184, -0.289373, 0.120182, -0.215965, 0.0989499, 0.0113317, 0.618816, 0.163347, 0.183448, 0.0309493, -0.0453121, -0.0512223, -0.102301, -0.209695},
                {0.635509, 0.0798913, 0.156008, 0.411777, 0.250392, 0.287913, -0.0921334, -0.352706, -0.0782517, 0.115808, -0.0476165, -0.0067602, -0.114485, 0.122168, 0.057887, 0.0742357, -0.0406523, -0.0390177, -0.127913, -0.19771},
                {-0.300667, -0.279992, 0.113915, -0.168213, 0.0677439, -0.0488945, 0.238999, -0.332378, 0.0588183, -0.128177, -0.0301665, 0.319267, -0.542735, 0.238236, 0.109672, 0.0975078, -0.0837083, -0.064048, -0.227195, -0.238841},
                {0.0130745, -0.0149929, -0.00561798, -0.0124797, -0.00262138, -0.00387919, -0.0320821, -0.00536147, -0.00333756, -0.0340044, -0.0209113, 0.102402, 0.00374295, 0.0309679, 0.0892624, 0.160805, 0.962237, 0.0523145, 0.0591768, -0.138953},
                {-0.145196, -0.437063, 0.307298, 0.527208, -0.204505, -0.135402, -0.267201, 0.401415, -0.0676391, -0.0354041, -0.0304356, 0.0987403, 0.123234, 0.133085, 0.144188, 0.046183, -0.0635984, -0.0097873, -0.0981483, -0.191646},
                {0.275955, 0.479893, -0.300913, -0.0775144, -0.27351, -0.179706, -0.12759, 0.398931, 0.0799185, -0.166597, 0.0452202, 0.207994, -0.260915, 0.200249, 0.151317, 0.0584104, -0.0802676, -0.0276578, -0.187797, -0.240954},
                {0.0019565, -0.00959573, 0.012179, -0.0380616, -0.0403913, -0.0237517, -0.0890043, -0.0174895, -0.0145572, -0.0876708, -0.0744615, 0.115719, 0.128676, -0.157692, -0.784393, -0.144388, 0.0738512, -0.196981, -0.394967, -0.288534},
                {-0.0594544, 0.0579705, -0.0789625, -0.197967, 0.0100792, -0.0165229, 0.084947, 0.0563854, -0.700934, 0.212782, -0.539484, 0.00867721, 0.148312, 0.0924781, 0.0847184, 0.193178, -0.0673019, -0.0706511, -0.0145201, -0.156305},
                {0.0607107, -0.359249, -0.640783, 0.142059, -0.00625714, -0.00901229, -0.113535, -0.149422, -0.0876775, -0.333833, -0.132103, -0.168475, -0.0718565, 0.0432696, -0.0574695, -0.224415, -0.0291425, 0.258126, 0.243968, -0.22015},
                {0.00845655, 0.0262534, 0.0390159, -0.0495443, -0.16698, 0.202981, 0.0798766, 0.0103444, 0.20792, 0.434061, -0.0872887, 0.515503, 0.114879, 0.0282272, -0.0915908, -0.228724, -0.0755265, 0.299606, 0.397888, -0.293614},
                {-0.188916, -0.0119332, -0.0533255, -0.336553, 0.353076, 0.456244, -0.386837, 0.0717068, 0.12167, -0.144364, 0.144141, 0.00552066, 0.359525, 0.201981, 0.202653, 0.0441587, -0.0737981, -0.01886, -0.151972, -0.249055},
                {-0.00146171, 0.111945, 0.0823599, -0.00655301, 0.644731, -0.674245, -0.166441, 0.0146814, 0.0590637, 0.0864678, -0.0297748, 0.0713814, 0.0396591, 0.0324164, -0.0200865, -0.101331, -0.0264909, 0.123327, 0.129543, -0.139652},
                {0.0110452, -0.00497841, 0.0104707, -0.00382676, 0.00799254, -0.0108164, -0.00139677, -0.011253, -0.365797, -0.118086, 0.599695, 0.133216, -0.0254481, -0.0986171, -0.202248, 0.43184, -0.105321, -0.107068, 0.418969, -0.196041},
                {0.0259049, -0.0063692, 0.0116422, -0.0186387, 0.00704533, -0.002765, -0.0489739, -0.0582945, -0.00483031, -0.13117, -0.0765354, 0.173156, -0.00194067, -0.862432, 0.357808, 0.00634591, -0.0585606, 0.0366786, -0.132229, -0.213923},
                {-0.5387, 0.384241, -0.248377, 0.438681, 0.0079612, 0.0547083, 0.0415592, -0.1462, 0.015551, 0.329598, 0.13995, -0.248686, -0.0559828, -0.0393576, 0.0371575, 0.018413, 0.0245784, -0.0113177, -0.122108, -0.263662},
                {0.13273, -0.161554, 0.135447, -0.366263, -0.382262, -0.242528, -0.35762, -0.257348, 0.00382218, 0.360852, 0.176422, -0.402303, -0.0667726, 0.00545648, 0.0796268, -0.0388068, -0.000495296, 0.0658128, -0.0620043, -0.247008},
                {0.00437012, -0.00608196, -0.00106697, -0.00488827, 0.00675325, 0.00838122, -0.014099, 0.008612, -0.0336306, 0.00747777, 0.00838118, 0.00168732, -0.0529232, 0.0127369, 0.173769, -0.48413, 0.0709289, -0.786865, 0.303572, -0.119941},
                {-0.00217725, -0.0161633, 0.00983414, -0.00461742, -0.0159307, -0.00249796, -0.0214375, 0.0361123, 0.502597, -0.0367827, -0.424703, -0.233143, -0.0255701, -0.0578968, -0.120656, 0.540064, -0.0944799, -0.245835, 0.306658, -0.187814},
                {-0.108615, 0.325121, 0.516757, -0.0303636, -0.0656181, 0.119651, 0.0839027, 0.0363258, -0.120166, -0.438825, -0.117463, -0.308547, -0.124544, 0.0323586, -0.0478674, -0.248597, 0.00580004, 0.270759, 0.216013, -0.266262}};

        this.freqs = new double[]{
                0.0866279,
                0.043972,
                0.0390894,
                0.0570451,
                0.0193078,
                0.0367281,
                0.0580589,
                0.0832518,
                0.0244313,
                0.048466,
                0.086209,
                0.0620286,
                0.0195027,
                0.0384319,
                0.0457631,
                0.0695179,
                0.0610127,
                0.0143859,
                0.0352742,
                0.0708956};

        init();
    }
}
