package nightgames.characters.body.mods;

import nightgames.characters.Character;
import nightgames.characters.body.BodyPart;
import nightgames.characters.body.BodyPartMod;
import nightgames.characters.body.CockMod;
import nightgames.characters.body.ModdedCockPart;
import nightgames.combat.Combat;
import nightgames.global.Global;
import nightgames.status.Shamed;

public class CyberneticHoleMod extends HoleMod {
    public CyberneticHoleMod() {
        super("cybernetic", -.1, .8, -.5, 0);
    }

    public double applyBonuses(Combat c, Character self, Character opponent, BodyPart part, BodyPart target, double damage) {
        int bonus = 0;
        if (target.isType("cock")) {
            if (target.getMod(opponent).countsAs(opponent, CockMod.enlightened)) {
                c.write(self, String.format(
                                "Despite %s %s's best efforts, %s focus does not waver, and %s barely %s a thing.",
                                self.nameOrPossessivePronoun(), part.describe(self), opponent.nameOrPossessivePronoun(),
                                opponent.pronoun(), opponent.human() ? "feel" : "feels"));
                bonus -= 5;
            } else {
                if (Global.random(3) == 0 || target.getMod(opponent).countsAs(opponent, CockMod.incubus)) {
                    String prefix = target.getMod(opponent).countsAs(opponent, CockMod.incubus) ? "Eager to gain a sample of "
                                    + opponent.nameOrPossessivePronoun() + " exotic, demonic sperm, " : "";
                    c.write(self, String.format(
                                    prefix + "%s %s whirls to life and starts attempting to extract all the semen packed inside %s %s. "
                                                    + "At the same time, %s feel a thin filament sliding into %s urethra, filling %s with both pleasure and shame.",
                                    self.possessiveAdjective(), part.describe(self), opponent.possessiveAdjective(),
                                    target.describe(opponent), opponent.pronoun(), opponent.possessiveAdjective(),
                                    opponent.directObject()));
                    bonus += 15;
                    if (target.getMod(opponent).countsAs(opponent, CockMod.incubus) || Global.random(4) == 0) {
                        opponent.add(c, new Shamed(opponent));
                    }
                }
            }
        }
        return bonus;
    }

    public int counterValue(BodyPart part, BodyPart otherPart, Character self, Character other) { 
        if (otherPart instanceof ModdedCockPart) {
            BodyPartMod mod = otherPart.getMod(self);
            return mod.countsAs(other, CockMod.incubus) ? 1 : mod.countsAs(other, CockMod.enlightened) ? -1 : 0;
        }
        return 0;
    }
}
