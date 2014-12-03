/*
 * Palo Open Office Calc AddIn
 * Copyright (C) 2008 PalOOCa Team,  Tensegrity Software GmbH, 2009

 * The software is licensed under an Open-Source License (GPL).
 * If you want to redistribute the software you must observe the regulations of
 * the GPL . If you want to redistribute the software without the
 * restrictions of the GPL, you have to contact Tensegrity Software GmbH
 * (Tensegrity) for written consent to do so.
 * Tensegrity may offer commercial licenses for redistribution (Dual Licensing)
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.palooca.dialogs.nodes;

import javax.swing.tree.DefaultMutableTreeNode;
import com.jedox.palojlib.interfaces.ICube;
import com.jedox.palojlib.interfaces.IRule;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author MichaelRaue
 */
public class CubeRulesTreeNode  extends DefaultMutableTreeNode {

    public class RuleProxy implements IRule {

        private int identifier;
        private String definition;
        private String externalIdentifier;
        private long timestamp;
        private String comment;
        private boolean active;

        public RuleProxy(int identifier,String definition,String externalIdentifier,String comment,long timestamp,boolean active){
            this.identifier = identifier;
            this.definition = definition;
            this.externalIdentifier = externalIdentifier;
            this.comment = comment;
            this.timestamp = timestamp;
            this.active = active;
        }

        @Override
        public int getIdentifier() {
            return identifier;
        }

        @Override
        public String getDefinition() {
            return definition;
        }

        @Override
        public String getExternalIdentifier() {
            return externalIdentifier;
        }

        @Override
        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String getComment() {
            return comment;
        }

        @Override
        public boolean isActive() {
            return active;
        }

        public void setIdentifier(int identifier) {
            this.identifier = identifier;
        }
    }

    private ICube cube;
    private List<RuleProxy> rules = new ArrayList<RuleProxy>();

    public ICube getCube() {
        return cube;
    }

    public CubeRulesTreeNode(ICube cube, String title) {
        super(title);
        this.cube = cube;
        for (IRule r : cube.getRules()) {
            RuleProxy rule = new RuleProxy(r.getIdentifier(),r.getDefinition(),r.getExternalIdentifier(),r.getComment(),r.getTimestamp(),r.isActive());
            rules.add(rule);
        }
    }

    public RuleProxy newRule() {
        RuleProxy rule = new RuleProxy(-1,"[] = []","","",0,true);
        rules.add(rule);
        return rule;
    }

    public RuleProxy updateRule(int pos, String definition, String comment) {
        RuleProxy rule = rules.get(pos);
        rule.definition = definition;
        rule.comment = comment;
        return rule;
    }

    public List<RuleProxy> getRules() {
        return rules;
    }
}
