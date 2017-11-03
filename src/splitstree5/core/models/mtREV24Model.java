/*
 *  Copyright (C) 2017 Daniel H. Huson
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
 *         <p/>
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class mtREV24Model extends ProteinModel {


    public mtREV24Model() {

		/*
         * Protein evolution matrix based on
		 * Adachi, J. and Hasegawa, M. (1996) MOLPHY version 2.3: programs for
		 *	molecular phylogenetics based on maximum likelihood.  Computer Science
		 *	Monographs of Institute of Statistical Mathematics 28:1-150.
		 *
		 * The matrix was recovered from the .dat files circulated with PAML 3.13d
		 */

		/* For efficiency, we precompute the eigenvalue decomposition 
         *     V'DV = Pi^(1/2) Q Pi(-1/2)
		 * We used matlab for this.
		 */

        this.evals = new double[]{
                -2.29832,
                -2.28222,
                -1.91358,
                -1.50894,
                -1.21349,
                -1.148,
                -0.962765,
                -0.862839,
                -0.761745,
                -0.74645,
                -0.657702,
                -0.589436,
                -0.54377,
                -0.453543,
                -0.418144,
                -0.267346,
                -0.218593,
                -0.196406,
                -0.134451,
                8.27664e-17};

        this.evecs = new double[][]{{-0.0752456, 0.0115214, 0.113059, 0.426837, -0.387, 0.264851, -0.00832704, 0.484975, 0.128985, -0.395141, 0.129047, 0.132701, -0.16047, 0.157837, 0.0299242, 0.0794623, -0.0205298, -0.0633857, 0.0519588, -0.268328},
                {-0.000421547, 6.51682e-05, -0.00244868, -0.00137675, 0.0108425, -0.0103538, -0.00916756, -0.000580516, 0.0230382, -0.0291122, 0.00815732, -0.0917996, 0.0065744, 0.100344, -0.0563352, 0.199605, 0.068977, 0.955387, 0.0126822, -0.13784},
                {-0.00477962, 0.00112169, -0.0223103, 0.530252, 0.493728, -0.505732, 0.14588, -0.0181141, 0.175113, -0.0470158, -0.0156417, 0.052022, -0.27272, -0.155882, 0.0653663, -0.133608, -0.00810148, 0.0121116, 0.0365084, -0.197484},
                {0.00024119, -0.000653532, 0.000742913, -0.0885, -0.139214, 0.165149, -0.0981134, 0.00455284, -0.12033, 0.178563, -0.061529, -0.526836, -0.659189, -0.0751823, -0.0233998, -0.376322, -0.0688997, 0.0349293, 0.0290592, -0.13784},
                {-0.00320379, -0.00606631, -0.000136829, 0.0392829, -0.0114869, 0.0305985, 0.0184813, -0.0778399, -0.065959, 0.310321, 0.933657, 0.0613367, -0.0199569, -0.0892219, 0.00666619, 0.0119749, 0.0129883, 0.00478693, 0.00864415, -0.0774597},
                {-0.00783915, 0.00284872, 0.012437, 0.0101829, 0.0254054, -0.0282066, -0.279274, 0.393809, -0.487093, 0.378694, -0.184077, 0.473897, 0.0196013, -0.202281, 0.116415, -0.180242, 0.0286677, 0.107679, 0.0355223, -0.158114},
                {-0.000265376, 0.000865676, -0.00584977, 0.0143332, 0.0156298, -0.0258305, 0.0383685, -0.0229144, -0.0023438, -0.139116, 0.083608, -0.0183598, 0.417146, 0.321401, -0.199892, -0.781177, -0.08955, 0.0915177, 0.0476149, -0.154919},
                {0.00676631, -0.000109723, -0.0197529, 0.00615814, 0.0100224, 0.012544, -0.00699902, -0.0714387, -0.0428749, 0.0659627, -0.0411891, -0.0285844, 0.102711, -0.048428, -0.0328559, 0.187398, -0.936177, -0.00356722, 0.0622928, -0.236643},
                {-2.95657e-05, -0.00166507, -0.000380446, -0.0682168, -0.13195, 0.19905, 0.572374, -0.294466, -0.296762, -0.321363, -0.029738, 0.198985, -0.0332899, -0.486769, 0.0575995, -0.117263, 0.045388, 0.0770048, 0.0325332, -0.167332},
                {-0.418642, 0.633883, -0.0327847, -0.0165658, 0.317656, 0.273227, -0.0938113, -0.194928, -0.116234, -0.00737004, -0.0344096, 0.041993, -0.0362868, 0.186566, -0.129343, 0.123158, 0.119386, -0.0998722, 0.0496502, -0.296648},
                {0.218428, 0.0525059, 0.216472, 0.132677, -0.454965, -0.388984, 0.020955, -0.309148, -0.171523, 0.212096, -0.12144, -0.0589234, 0.0710643, 0.173406, -0.21372, 0.176839, 0.190752, -0.134244, 0.0515324, -0.411096},
                {-0.00150575, 0.00452121, 0.0259761, -0.0593288, -0.168304, 0.223807, -0.0340394, -0.247557, 0.688708, 0.341216, -0.141275, 0.40669, -0.102998, -0.0966072, 0.0604849, -0.165425, 0.00399672, 0.064376, 0.0263676, -0.151658},
                {-0.566714, -0.624362, -0.413313, -0.0423642, -0.0290818, -0.0394671, -0.0285511, -0.104328, -0.0549184, 0.0160255, -0.0328727, 0.0284062, -0.021681, 0.115856, -0.0874031, 0.085262, 0.0890238, -0.0723333, 0.0340924, -0.232379},
                {0.000398315, -0.00968227, -0.0196607, 0.00378552, 0.129867, 0.160767, 0.350534, 0.431161, 0.193696, 0.332944, -0.089512, -0.371965, 0.346017, -0.287767, -0.230749, 0.0926284, 0.143115, -0.0772431, 0.0383331, -0.246982},
                {-0.0106487, -0.00453265, 0.00142792, 0.0460805, -0.00447398, 0.0568653, -0.0135098, -0.0919673, -0.00867535, 0.0520203, -0.0237438, -0.260614, 0.246339, 0.102709, 0.883783, -0.0181008, 0.0704057, -0.0183791, 0.0515261, -0.232379},
                {-0.164088, -0.0397475, 0.444168, -0.644171, 0.102423, -0.332842, 0.075873, 0.240295, 0.14211, -0.188459, 0.102123, 0.0806064, -0.135494, 0.037225, 0.078412, 0.0112562, -0.0103729, -0.0452633, 0.0408251, -0.268328},
                {0.521757, 0.121149, -0.66895, -0.282819, 0.062801, -0.0484477, -0.00765764, 0.12292, 0.0546219, -0.148128, 0.0561944, 0.0985386, -0.113359, 0.11454, 0.0119875, 0.0583421, 0.0588983, -0.0699355, 0.0498301, -0.293258},
                {-2.03835e-05, 0.00179765, -0.00314773, 0.00524414, 0.00607417, 0.0108715, 0.00201201, 0.00642471, -0.00400579, -0.00383134, -0.00319036, 0.00257873, 0.0054178, 0.00296638, 0.0130183, -0.0227192, -0.0272288, -0.00404328, -0.984495, -0.170294},
                {0.00156835, 0.000928372, 0.00331305, -0.00267267, -0.0389508, -0.0465167, -0.638766, -0.120308, 0.120449, -0.327215, 0.0670703, -0.166569, 0.218292, -0.566582, -0.0980021, -0.0232667, 0.086075, -0.0113811, 0.0250858, -0.181659},
                {0.388312, -0.434775, 0.34858, 0.044245, 0.44552, 0.427118, -0.115586, -0.153309, -0.102979, -0.0490911, -0.0208717, 0.054175, -0.045869, 0.164563, -0.0952521, 0.0898189, 0.0822448, -0.0717867, 0.0361677, -0.207364}};

        this.freqs = new double[]{
                0.072,
                0.019,
                0.039,
                0.019,
                0.006,
                0.025,
                0.024,
                0.056,
                0.028,
                0.088,
                0.169,
                0.023,
                0.054,
                0.061,
                0.054,
                0.072,
                0.086,
                0.029,
                0.033,
                0.043};

        init();
    }
}
