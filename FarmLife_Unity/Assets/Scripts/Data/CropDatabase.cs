using UnityEngine;
using System.Collections.Generic;
using Unity.VisualScripting;

[CreateAssetMenu(fileName = "CropDatabase", menuName = "FarmGame/Crop Database")]
public class CropDatabase : ScriptableObject
{
    [SerializeField] private List<CropData> allCrops = new List<CropData>();

    public CropData GetCropBySeed(ItemData seed)
    {
        foreach (var crop in allCrops)
        {
            if (crop.seedItem == seed)
            {
                return crop;
            }
        }
        return null;
    }
}
