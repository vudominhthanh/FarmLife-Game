using UnityEngine;

public class HotbarInput : MonoBehaviour 
{
    [SerializeField] private InventoryManager manager;
    [SerializeField] private PlayerItemUser itemUser;

    private void Update()
    {
        if (manager == null) return;

        for (int i = 0; i < 10; i++)
        {
            KeyCode key = i == 9 ? KeyCode.Alpha0 : KeyCode.Alpha1 + i;
            if (Input.GetKeyDown(key)) manager.SelectHotbar(i);
        }

        float scroll = Input.mouseScrollDelta.y;
        if (scroll > 0.1f) Cycle(-1);
        else if (scroll < -0.1f) Cycle(+1);

        if (Input.GetKeyDown(KeyCode.E) && itemUser != null)
                itemUser.UseSelected();
    }

    private void Cycle(int dir)
    {
        int size = manager.Hotbar.Capacity;
        int next = (manager.SelectedHotbarIndex + dir + size) % size;
        manager.SelectHotbar(next);
    }
}
