package nightgames.skills;

import java.util.Arrays;
import java.util.Map;

import nightgames.characters.Attribute;
import nightgames.characters.Character;
import nightgames.characters.Trait;
import nightgames.characters.body.BodyPart;
import nightgames.characters.body.CockMod;
import nightgames.combat.Combat;
import nightgames.combat.Result;
import nightgames.global.Global;
import nightgames.nskills.tags.SkillTag;
import nightgames.pet.arms.skills.Grab;
import nightgames.skills.damage.DamageType;
import nightgames.stance.Neutral;
import nightgames.stance.Position;
import nightgames.stance.Stance;
import nightgames.status.Bound;
import nightgames.status.CockBound;
import nightgames.status.Collared;
import nightgames.status.MagLocked;
import nightgames.status.Stsflag;

public class Struggle extends Skill {

    public Struggle(Character self) {
        super("Struggle", self);
        addTag(SkillTag.positioning);
        addTag(SkillTag.escaping);
    }

    @Override
    public boolean usable(Combat c, Character target) {
        if (!getSelf().canRespond()) {
            return false;
        }
        if (target.hasStatus(Stsflag.cockbound) || target.hasStatus(Stsflag.knotted)) {
            return false;
        }
        if (getSelf().hasStatus(Stsflag.cockbound) || getSelf().hasStatus(Stsflag.knotted)) {
            return getSelf().canRespond();
        }
        return ((!c.getStance().mobile(getSelf()) && !c.getStance().dom(getSelf()) || getSelf().bound()
                        || getSelf().is(Stsflag.maglocked))
                        || hasSingleGrabber(c, target))
                        && getSelf().canRespond();
    }

    @Override
    public boolean resolve(Combat c, Character target) {
        if (blockedByCollar(c, target)) {
            return false;
        }
        if (getSelf().is(Stsflag.maglocked)) {
            return struggleMagLock(c, target);
        } else if (hasSingleGrabber(c, target)) {
            return struggleGrabber(c, target);
        } else if (getSelf().bound()) {
            return struggleBound(c, target);
        } else if (c.getStance().havingSex(c)) {
            boolean knotted = getSelf().hasStatus(Stsflag.knotted);
            if (c.getStance().enumerate() == Stance.anal) {
                return struggleAnal(c, target, knotted);
            } else {
                return struggleVaginal(c, target, knotted);
            }
        } else {
            return struggleRegular(c, target);
        }
    }
    
    private boolean hasSingleGrabber(Combat c, Character target) {
        return c.getCombatantData(target).getIntegerFlag(Grab.FLAG) == 1;
    }
    
    private boolean blockedByCollar(Combat c, Character target) {
        Collared stat = (Collared) getSelf().getStatus(Stsflag.collared);
        if (stat != null) {
            c.write(getSelf(), Global.format("{self:SUBJECT-ACTION:try|tries} to struggle, but"
                            + " the collar is having none of it and shocks {self:direct-object}"
                            + " into submission.", getSelf(), target));
            getSelf().pain(c, null, Global.random(20, 50));
            stat.spendCharges(c, 2);
            return true;
        }
        return false;
    }

    private boolean struggleBound(Combat c, Character target) {
        Bound status = (Bound) target.getStatus(Stsflag.bound);
        if (getSelf().check(Attribute.Power, -getSelf().escape(c, target))) {
            if (getSelf().human()) {
                if (status != null) {
                    c.write(getSelf(), "You manage to break free from the " + status + ".");
                } else {
                    c.write(getSelf(), "You manage to snap the restraints that are binding your hands.");
                }
            } else if (c.shouldPrintReceive(target, c)) {
                if (status != null) {
                    c.write(getSelf(), getSelf().getName() + " slips free from the " + status + ".");
                } else {
                    c.write(getSelf(), getSelf().getName() + " breaks free.");
                }
            }
            getSelf().free();
            c.getCombatantData(target).setIntegerFlag(Grab.FLAG, 0);
            return true;
        } else {
            if (getSelf().human()) {
                if (status != null) {
                    c.write(getSelf(), "You struggle against the " + status + ", but can't get free.");
                } else {
                    c.write(getSelf(), "You struggle against your restraints, but can't get free.");
                }
            } else if (c.shouldPrintReceive(target, c)) {
                if (status != null) {
                    c.write(getSelf(), getSelf().getName() + " struggles against the " + status
                                    + ", but can't free her hands.");
                } else {
                    c.write(getSelf(), getSelf().getName() + " struggles, but can't free her hands.");
                }
            }
            getSelf().struggle();
            return false;
        }
    }

    private boolean struggleAnal(Combat c, Character target, boolean knotted) {
        int diffMod = knotted ? 50 : 0;
        if (target.has(Trait.grappler))
            diffMod += 15;
        if (getSelf().check(Attribute.Power,
                        target.getStamina().get() / 2 - getSelf().getStamina().get() / 2
                                        + target.get(Attribute.Power) - getSelf().get(Attribute.Power)
                                        - getSelf().escape(c, target) + diffMod)) {
            if (c.getStance().reverse(c, true) != c.getStance()) {
                c.setStance(c.getStance().reverse(c, false));
            } else if (getSelf().human()) {
                if (knotted) {
                    c.write(getSelf(), "With a herculean effort, you painfully force "
                                    + target.possessiveAdjective()
                                    + " knot through your asshole, and the rest of her dick soon follows.");
                    getSelf().removeStatus(Stsflag.knotted);
                    target.pain(c, getSelf(), (int) getSelf().modifyDamage(DamageType.physical, target, 10));
                } else {
                    c.write(getSelf(), "You manage to break away from " + target.getName() + ".");
                }
            } else if (c.shouldPrintReceive(target, c)) {
                if (knotted) {
                    c.write(getSelf(), String.format("%s roughly pulls away from %s, groaning loudly"
                                    + " as the knot in %s dick pops free of %s ass.", getSelf().subject(),
                                    target.nameDirectObject(), target.possessiveAdjective(),
                                    getSelf().possessiveAdjective()));
                    getSelf().removeStatus(Stsflag.knotted);
                    target.pain(c, getSelf(), (int) getSelf().modifyDamage(DamageType.physical, target, 10));
                } else {
                    c.write(getSelf(), String.format("%s pulls away from %s and"
                                    + " %s dick slides out of %s butt.",
                                    getSelf().subject(), target.nameDirectObject(),
                                    target.possessiveAdjective(), getSelf().possessiveAdjective()));
                }
            }
            c.setStance(new Neutral(getSelf(), target));
            return true;
        } else {
            if (getSelf().human()) {
                if (knotted) {
                    c.write(getSelf(), "You try to force " + target.possessiveAdjective()
                                    + " dick out of your ass, but the knot at its base is utterly unyielding.");
                } else {
                    c.write(getSelf(), "You try to pull free, but " + target.getName()
                                    + " has a good grip on your waist.");
                }
            } else if (c.shouldPrintReceive(target, c)) {
                if (knotted) {
                    c.write(getSelf(),
                                    String.format("%s frantically attempts to get %s cock out of %s ass, "
                                                    + "but %s knot is keeping it inside %s warm depths.",
                                                    getSelf().subject(), target.nameOrPossessivePronoun(),
                                                    getSelf().possessiveAdjective(), target.possessiveAdjective(),
                                                    getSelf().possessiveAdjective()));
                } else {
                    c.write(getSelf(), String.format("%s tries to squirm away, but %s better leverage.",
                                    getSelf().subject(), target.subjectAction("have", "has")));
                }
            }
            getSelf().struggle();
            return false;
        }
    }

    private boolean struggleVaginal(Combat c, Character target, boolean knotted) {
        int diffMod = 0;
        Character partner;
        if (c.getStance().sub(getSelf())) {
            partner = c.getStance().domSexCharacter(c);
        } else {
            partner = c.getStance().bottom;
        }
        target = partner;
        if (c.getStance().insertedPartFor(c, target).moddedPartCountsAs(target, CockMod.enlightened)) {
            diffMod = 15;
        } else if (c.getStance().insertedPartFor(c, getSelf()).moddedPartCountsAs(getSelf(), CockMod.enlightened)) {
            diffMod = -15;
        }
        if (target.has(Trait.grappler)) {
            diffMod += 15;
        }
        if (getSelf().check(Attribute.Power,
                        target.getStamina().get() / 2 - getSelf().getStamina().get() / 2
                                        + target.get(Attribute.Power) - getSelf().get(Attribute.Power)
                                        - getSelf().escape(c, target) + diffMod)) {
            if (getSelf().hasStatus(Stsflag.cockbound)) {
                CockBound s = (CockBound) getSelf().getStatus(Stsflag.cockbound);
                c.write(getSelf(),
                                Global.format("With a strong pull, {self:subject} somehow managed to wiggle out of {other:possessive} iron grip on {self:possessive} dick. "
                                                + "However the sensations of " + s.binding
                                                + " sliding against {self:possessive} cockskin leaves {self:direct-object} gasping.",
                                getSelf(), target));
                int m = 15;
                getSelf().body.pleasure(target, target.body.getRandom("pussy"),
                                getSelf().body.getRandom("cock"), m, c, this);
                getSelf().removeStatus(Stsflag.cockbound);
            }
            if (knotted) {
                c.write(getSelf(),
                                Global.format("{self:subject} somehow {self:SUBJECT-ACTION:manage|manages} to force {other:possessive} knot through {self:possessive} tight opening, stretching it painfully in the process.",
                                                getSelf(), target));
                getSelf().removeStatus(Stsflag.knotted);
                getSelf().pain(c, getSelf(), 10);
            }
            boolean reverseStrapped = BodyPart.hasOnlyType(c.getStance().partsFor(c, target), "strapon");
            boolean reversedStance = false;
            if (!reverseStrapped) {
                Position reversed = c.getStance().reverse(c, true);
                if (reversed != c.getStance()) {
                    c.setStance(reversed);
                    reversedStance = true;
                }
            }
            if (!reversedStance) {
                c.write(getSelf(),
                                Global.format("{self:SUBJECT-ACTION:manage|manages} to shake {other:direct-object} off.",
                                                getSelf(), target));
                c.setStance(new Neutral(getSelf(), target));
            }
            return true;
        } else {
            if (getSelf().hasStatus(Stsflag.cockbound)) {
                CockBound s = (CockBound) getSelf().getStatus(Stsflag.cockbound);
                c.write(getSelf(),
                                Global.format("{self:SUBJECT-ACTION:try|tries} to escape {other:possessive} iron grip on {self:possessive} dick. However, {other:possessive} "
                                                + s.binding
                                                + " has other ideas. {other:SUBJECT-ACTION:run|runs} {other:possessive} "
                                                + s.binding
                                                + " up and down {self:possessive} cock and leaves {self:direct-object} gasping with pleasure.",
                                getSelf(), target));
                getSelf().body.pleasure(target, target.body.getRandom("pussy"),
                                getSelf().body.getRandom("cock"), 8, c, this);
            } else if (getSelf().human()) {
                if (c.getStance().inserted(getSelf())) {
                    c.write(getSelf(), "You try to tip " + target.getName()
                                    + " off balance, but she drops her hips firmly, pushing your cock deep inside her and pinning you to the floor.");
                } else {
                    if (knotted) {
                        c.write(getSelf(), "You struggle fruitlessly against the lump of "
                                        + target.nameOrPossessivePronoun() + " knot.");
                    } else {
                        c.write(getSelf(), "You attempt to get away from " + target.getName()
                                        + ", but she drives her cock into you to the hilt, pinning you down.");
                    }
                }
            } else if (c.shouldPrintReceive(target, c)) {
                if (c.getStance().behind(target)) {
                    c.write(getSelf(), String.format("%s struggles to gain a more dominant position, but with"
                                    + " %s behind %s holding %s waist firmly, there is nothing %s can do.",
                                    getSelf().subject(), target.subject(), getSelf().directObject(),
                                    getSelf().possessiveAdjective(), getSelf().pronoun()));
                } else {
                    c.write(getSelf(), String.format("%s tries to roll on top of %s, but %s %s %s superior "
                                    + "upper body strength to maintain %s position.",
                                    getSelf().subject(), target.nameDirectObject(),
                                    target.pronoun(), target.action("use"), target.possessiveAdjective(),
                                    target.possessiveAdjective()));
                }
            }
            getSelf().struggle();
            return false;
        }
    }

    private boolean struggleRegular(Combat c, Character target) {
        if ((getSelf().check(Attribute.Power, target.getStamina().get() / 2 - getSelf().getStamina().get() / 2
                        + target.get(Attribute.Power) - getSelf().get(Attribute.Power) - getSelf().escape(c, target)))
                        && (!target.has(Trait.grappler) || Global.random(10) >= 2)) {
            if (getSelf().human()) {
                c.write(getSelf(), "You manage to scrabble out of " + target.getName() + "'s grip.");
            } else if (c.shouldPrintReceive(target, c)) {
                c.write(getSelf(), getSelf().getName() + " squirms out from under "+target.nameDirectObject()+".");
            }
            c.setStance(new Neutral(getSelf(), target));
            return true;
        } else {
            if (c.getStance().enumerate() == Stance.facesitting) {
                if (getSelf().human()) {
                    c.write(getSelf(), "You try to free yourself from " + target.getName()
                                    + ", but she drops her ass over your face again, forcing you to service her.");
                } else if (c.shouldPrintReceive(target, c)) {
                    c.write(getSelf(), String.format("%s struggles against %s, but %s %s %s ass "
                                    + "over %s face again, forcing %s to service %s.", getSelf().subject(),
                                    target.nameDirectObject(), target.pronoun(), target.action("drop"),
                                    target.possessiveAdjective(), getSelf().possessiveAdjective(),
                                    getSelf().directObject(), target.directObject()));
                }
                if (target.hasPussy()) {
                    new Cunnilingus(getSelf()).resolve(c, target);
                } else {
                    new Anilingus(getSelf()).resolve(c, target);
                }
                target.weaken(c, (int) getSelf().modifyDamage(DamageType.physical, target, Global.random(5, 10)));
                getSelf().struggle();
                return false;
            } else {
                if (getSelf().human()) {
                    c.write(getSelf(), "You try to free yourself from " + target.getName()
                                    + "'s grasp, but she has you pinned too well.");
                } else if (c.shouldPrintReceive(target, c)) {
                    c.write(getSelf(), String.format("%s struggles against %s, but %s %s %s position.",
                                    getSelf().subject(), target.nameDirectObject(), target.pronoun(),
                                    target.action("maintain"), target.possessiveAdjective()));
                }
                target.weaken(c, (int) getSelf().modifyDamage(DamageType.physical, target, Global.random(5, 10)));
                getSelf().struggle();
                return false;
            }
        }
    }
    
    private boolean struggleMagLock(Combat c, Character target) {
        MagLocked stat = (MagLocked) getSelf().getStatus(Stsflag.maglocked);
        
        Attribute highestAdvancedAttr = null;
        int attrLevel = 0;
        for (Map.Entry<Attribute, Integer> ent : getSelf().att.entrySet()) {
            Attribute attr = ent.getKey();
            if (attr == Attribute.Power || attr == Attribute.Seduction || attr == Attribute.Cunning) {
                continue;
            }
            if (ent.getValue() > attrLevel) {
                highestAdvancedAttr = attr;
                attrLevel = ent.getValue();
            }
        }
        
        boolean basic = highestAdvancedAttr == null;
        int dc;
        
        if (basic) {
           attrLevel = Math.max(getSelf().get(Attribute.Power), 
                           Math.max(getSelf().get(Attribute.Seduction), 
                                           getSelf().get(Attribute.Cunning))) / 2;        
        }
        dc = attrLevel + Global.random(-10, 20);
        
        // One MagLock, pretty easy to remove
        if (stat.getCount() == 1) {
            if (!target.check(Attribute.Science, dc * 2)) {
                c.write(getSelf(), Global.format("Still having one hand completely free, it's not to"
                            + " difficult for {self:subject} to remove the lone MagLock"
                            + " {other:subject} had placed around {self:possessive} wrist.", getSelf(), target));
                getSelf().removeStatus(stat);
                return true;
            } else {
                c.write(getSelf(), Global.format("{self:SUBJECT-ACTION:pull|pulls} at the MagLock around"
                                + " {self:possessive} wrist, but it's not budging.", getSelf(), target));
            }
        } else {
            if (stat.getCount() != 2) {
                // Three MagLocks? Shouldn't be able to struggle if that's the case...
                c.write("ERROR: Something went wrong with the MagLocks...");
                return false;
            }
            
            // Two MagLocks, difficult to remove
            if (!target.check(Attribute.Science, dc)) {
                String msg = "{self:SUBJECT-ACTION:struggle|struggles} against the powerful"
                                + " MagLocks locked around {self:possessive} wrists by ";
                if (Arrays.asList(Attribute.Dark, Attribute.Arcane, Attribute.Temporal, Attribute.Divinity)
                                .contains(highestAdvancedAttr)) {
                    msg += "trying to pry them of with {self:possessive} magic";
                } else if (Arrays.asList(Attribute.Power, 
                                Attribute.Ki, Attribute.Ninjutsu, Attribute.Animism, Attribute.Nymphomania)
                                .contains(highestAdvancedAttr)) {
                    msg += "applying brute force with {self:possessive} powerful muscles";
                } else if (Arrays.asList(Attribute.Cunning, Attribute.Science, Attribute.Hypnosis)
                                .contains(highestAdvancedAttr)) {
                    msg += "finding and exploiting a weakness in their design";
                } else {
                    msg += "twisting and turning {slef:possessive} hands as much as possible"
                                    + " while attempting to force them apart";
                }
                msg += ", and eventually succeeds. The two bands drop to the ground and power down.";
                if (!target.human()) {
                    msg += " {other:SUBJECT} seems very surprised that {self:subject} was able to escape.";
                }
                c.write(getSelf(), Global.format(msg, getSelf(), target));
                getSelf().removeStatus(stat);
                return true;
            } else {
                c.write(getSelf(), Global.format("{self:SUBJECT-ACTION:struggle|struggles} against the"
                                + " MagLocks around {self:possessive} wrist, but {self:action:prove|proves}"
                                + " no match for their insanely strong attraction.", getSelf(), target));
            }
        }
        
        return false;
    }
    
    private boolean struggleGrabber(Combat c, Character target) {
        int baseResist = Math.min(90, 40 + target.get(Attribute.Science));
        int trueResist = Math.max(20, baseResist) - getSelf().get(Attribute.Science) / 2 
                                                  - getSelf().get(Attribute.Power) / 3 
                                                  - getSelf().get(Attribute.Cunning) / 3;
        if (Global.random(100) > trueResist) {
            c.write(getSelf(), Global.format("{self:SUBJECT-ACTION:wrench|wrenches}"
                            + " {other:name-possessive} Grabber off {self:possessive}"
                            + " wrist without too much trouble.", getSelf(), target));
            c.getCombatantData(target).setIntegerFlag(Grab.FLAG, 0);
            return true;
        } else {
            c.write(getSelf(), Global.format("{self:SUBJECT-ACTION:pull|pulls} mightily"
                            + " on the Grabber around {self:possessive} wrist, but"
                            + " {self:action:fail|fails} to remove it.", getSelf(), target));
        }
        return false;
    }
    
    @Override
    public boolean requirements(Combat c, Character user, Character target) {
        return user.get(Attribute.Power) >= 3;
    }

    @Override
    public Skill copy(Character user) {
        return new Struggle(user);
    }

    @Override
    public int speed() {
        return 0;
    }

    @Override
    public Tactics type(Combat c) {
        return Tactics.positioning;
    }

    @Override
    public String deal(Combat c, int damage, Result modifier, Character target) {
        return null;
    }

    @Override
    public String receive(Combat c, int damage, Result modifier, Character target) {
        return null;
    }

    @Override
    public String describe(Combat c) {
        return "Attempt to escape a submissive position using Power";
    }

    @Override
    public boolean makesContact() {
        return true;
    }
}
