package real;

import boardGame.Place;
import com.fasterxml.jackson.databind.deser.impl.CreatorCandidate;
import io.Session;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import candybattle.CandyBattle;
import server.Service;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static threading.Manager.*;
import static real.ItemData.*;

public class Mob {

    public static final int THIEU_DOT_ID = 9;

    public boolean isFire;
    public boolean isIce;
    public boolean isWind;
    public boolean isBurn;

    public boolean isDisable;
    public boolean isDontMove;
    public long timeDisable;
    public long timeDontMove;

    public boolean isThieuDot;
    public Body masterThieuDot;

    public long timeFire;
    public long timeIce;
    public long timeWind;
    public long timeBurn;

    public int id;
    public byte sys;
    public int hp;
    public int level;
    public int hpmax;
    public short x;
    public short y;
    public byte status;
    public int lvboss;
    private boolean isboss;
    public volatile boolean isDie;
    public boolean isRefresh;
    public long xpup;
    private long timeRefresh;
    public long timeFight;
    public MobData templates;

    private short[] arrItemIds;

    public AtomicInteger attackCount = new AtomicInteger(0);

    @NotNull
    private final HashMap<@NotNull Integer, @NotNull Integer> nFight;

    public Mob(final int id, final int idtemplate, final int level) {
        this.isRefresh = true;
        this.id = id;
        this.templates = MobData.entrys.get(idtemplate);
        this.level = level;
        final int hp = this.templates.hp;
        this.hpmax = hp;
        this.hp = hp;
        this.xpup = 100000L;
        this.isDie = false;
        this.nFight = new HashMap<>();

        this.setArrItemIds();
    }

    public short[] getArrItemIds() {
        return this.arrItemIds;
    }

    private void setArrItemIds() {
        int curMobMaxLv = this.level - this.level % 10 + 10;
        if (curMobMaxLv > 100) {
            curMobMaxLv = 100;
        }

        switch (curMobMaxLv) {
            case 10:
                this.arrItemIds = ITEM_LV_10;
                break;
            case 20:
                this.arrItemIds = ITEM_LV_20;
                break;
            case 30:
                this.arrItemIds = ITEM_LV_30;
                break;
            case 40:
                this.arrItemIds = ITEM_LV_40;
                break;
            case 50:
                this.arrItemIds = ITEM_LV_50;
                break;
            case 60:
                this.arrItemIds = ITEM_LV_60;
                break;
            case 70:
                this.arrItemIds = ITEM_LV_70;
                break;
            case 80:
                this.arrItemIds = ITEM_LV_80;
                break;
            case 90:
                this.arrItemIds = ITEM_LV_90;
                break;
            case 100:
            case 110:
            case 120:
            case 130:
            case 140:
            case 150:
            case 160:
                this.arrItemIds = ITEM_LV_100;
                break;
            default:
                this.arrItemIds = new short[0];
        }
    }

    public void updateHP(final int num) {
        this.attackCount.incrementAndGet();
        this.hp += num;
        if (this.hp <= 0) {

            if (this.templates.id == CandyBattle.GIO_KEO_TRANG_ID
                    || this.templates.id == CandyBattle.GIO_KEO_DEN_ID) {
                this.hp = this.hpmax;
                this.isDie = false;

                return;
            }
            this.hp = 0;
            this.status = 0;
            this.isDie = true;
            if (this.isRefresh) {
                this.timeRefresh = System.currentTimeMillis() + TIME_REFRESH_MOB;
            }
            if (this.isboss) {
                if (this.templates.id != 198 && this.templates.id != 199 && this.templates.id != 200) {
                    this.isRefresh = false;
                    this.timeRefresh = -1L;
                } else {
                    this.timeRefresh = 10000L;
                }
            }
        }
    }

    public void ClearFight() {
        this.nFight.clear();
    }

    public int sortNinjaFight() {
        int idN = -1;
        int dameMax = 0;
        for (final int value : this.nFight.keySet()) {
            final int dame = this.nFight.get(value);
            final Session conn = PlayerManager.getInstance().getConn(value);
            if (conn != null && conn.user != null && conn.user.nj != null) {
                if (dame <= dameMax) {
                    continue;
                }
                dameMax = this.nFight.get(value);
                idN = conn.user.nj.id;
            }
        }
        return idN;
    }

    public void Fight(final int id, final int dame) {
        if (!this.nFight.containsKey(id)) {
            this.nFight.put(id, dame);
        } else {
            int damenew = this.nFight.get(id);
            damenew += dame;
            this.nFight.replace(id, damenew);
        }
    }

    public void removeFight(final int id) {
        this.nFight.remove(id);
    }

    public boolean isFight(final int id) {
        return this.nFight.containsKey(id);
    }

    public boolean isDisable() {
        return false;
    }

    public boolean isDonteMove() {
        return false;
    }

    public long getTimeRefresh() {
        return timeRefresh;
    }

    public void setTimeRefresh(long timeRefresh) {
        this.timeRefresh = timeRefresh;
    }

    public boolean isIsboss() {
        return isboss;
    }

    public void setIsboss(boolean isboss) {
        this.isboss = isboss;

        // update item.
        this.updateBossItemDrop();
    }

    private void updateBossItemDrop() {
        if (this.isboss && (this.templates.arrIdItem == null || this.templates.arrIdItem.length == 0)) {
            if (this.level == 45) {
                this.templates.arrIdItem = BOSS_ITEM_LV45;
            } else if (this.level == 55) {
                this.templates.arrIdItem = BOSS_ITEM_LV55;
            } else if (this.level == 60) {
                this.templates.arrIdItem = BOSS_ITEM_LV60;
            } else if (this.level == 65) {
                this.templates.arrIdItem = BOSS_ITEM_LV65;
            } else if (this.level == 75) {
                this.templates.arrIdItem = BOSS_ITEM_LV75;
            } else if (this.level == 90) {
                this.templates.arrIdItem = BOSS_ITEM_LV90;
            } else if (this.level == 100) {
                this.templates.arrIdItem = BOSS_ITEM_LV100;
            } else if (this.level == 99) {
                this.templates.arrIdItem = BOSS_ITEM_LV99;
            } else if (this.level == 110) {
                this.templates.arrIdItem = BOSS_ITEM_LV110;
            } else {
                this.templates.arrIdItem = BOSS_DEFAULT_ITEM;
            }
        }

    }

    @SneakyThrows
    public void update(final @NotNull Place place) {
        if (isThieuDot
                && masterThieuDot != null) {
            final Effect effId = masterThieuDot.getEffId(THIEU_DOT_ID);

            if (effId == null || System.currentTimeMillis() > effId.timeRemove) {
                this.masterThieuDot = null;
                this.isThieuDot = false;
            } else {
                Service.sendThieuDot(place.getUsers(), this.id);
                val dame = effId.param * this.masterThieuDot.dameMax() / 2000;
                this.updateHP(dame);
                val expUp = (long) this.level * dame / 1000;
                if (masterThieuDot instanceof Ninja) {
                    ((Ninja) masterThieuDot).p.updateExp(expUp, true);
                } else {
                    ((CloneChar) masterThieuDot).chuThan.p.updateExp(expUp / 2, true);
                }
                place.attackAMob(masterThieuDot, this, dame);
            }
        }
    }
}
