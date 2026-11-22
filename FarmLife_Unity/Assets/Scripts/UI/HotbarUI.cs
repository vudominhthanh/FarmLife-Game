using UnityEngine;

public class HotbarUI : MonoBehaviour
{
    [SerializeField] private InventoryManager manager;
    [SerializeField] private SlotUI[] slotUIs;

    private void Start()
    {
        if (manager == null) return;

        for (int i = 0; i < slotUIs.Length; i++)
            slotUIs[i].Bind(manager.Hotbar, i);

        manager.OnSelectedHotbarChanged += OnSelectionChanged;
        OnSelectionChanged(manager.SelectedHotbarIndex);
    }

    private void OnDestroy()
    {
        if (manager != null)
            manager.OnSelectedHotbarChanged -= OnSelectionChanged;
    }

    private void OnSelectionChanged(int index)
    {
        for (int i = 0; i < slotUIs.Length;i++)
            slotUIs[i].SetSelected(i == index);
    }
}
