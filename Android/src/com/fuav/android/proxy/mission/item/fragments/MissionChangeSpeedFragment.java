package com.fuav.android.proxy.mission.item.fragments;

import android.view.View;

import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.command.ChangeSpeed;

import org.beyene.sius.unit.composition.speed.SpeedUnit;
import com.fuav.android.R;
import com.fuav.android.utils.unit.providers.speed.SpeedUnitProvider;
import com.fuav.android.view.spinnerWheel.CardWheelHorizontalView;
import com.fuav.android.view.spinnerWheel.adapters.SpeedWheelAdapter;

public class MissionChangeSpeedFragment extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener<SpeedUnit> {

    @Override
    protected int getResource() {
        return R.layout.fragment_editor_detail_change_speed;
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        final View view = getView();
        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.CHANGE_SPEED));

        final SpeedUnitProvider speedUnitProvider = getSpeedUnitProvider();
        final SpeedWheelAdapter adapter = new SpeedWheelAdapter(getContext(), R.layout.wheel_text_centered,
                speedUnitProvider.boxBaseValueToTarget(0), speedUnitProvider.boxBaseValueToTarget(20));
        CardWheelHorizontalView<SpeedUnit> cardAltitudePicker = (CardWheelHorizontalView<SpeedUnit>) view.findViewById
                (R.id.picker1);
        cardAltitudePicker.setViewAdapter(adapter);
        cardAltitudePicker.addScrollListener(this);

        ChangeSpeed item = (ChangeSpeed) getMissionItems().get(0);
        cardAltitudePicker.setCurrentValue(speedUnitProvider.boxBaseValueToTarget(item.getSpeed()));
    }

    @Override
    public void onScrollingStarted(CardWheelHorizontalView cardWheel, SpeedUnit startValue) {

    }

    @Override
    public void onScrollingUpdate(CardWheelHorizontalView cardWheel, SpeedUnit oldValue, SpeedUnit newValue) {

    }

    @Override
    public void onScrollingEnded(CardWheelHorizontalView wheel, SpeedUnit startValue, SpeedUnit endValue) {
        switch (wheel.getId()) {
            case R.id.picker1:
                double baseValue = endValue.toBase().getValue();
                for (MissionItem missionItem : getMissionItems()) {
                    ChangeSpeed item = (ChangeSpeed) missionItem;
                    item.setSpeed(baseValue);
                }
                getMissionProxy().notifyMissionUpdate();
                break;
        }
    }
}
